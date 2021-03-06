package com.wmr.community.controller;

import com.google.code.kaptcha.Producer;
import com.wmr.community.service.UserService;
import com.wmr.community.util.CommunityConstant;
import com.wmr.community.util.CommunityUtil;
import com.wmr.community.util.CookieUtil;
import com.wmr.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
public class LoginController implements CommunityConstant {
    private final static Logger logger = LoggerFactory.getLogger(LoginController.class);
    private UserService userService;

    private Producer kaptchaProducer;

    private RedisTemplate<String, Object> redisTemplate;


    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    public void setKaptchaProducer(Producer kaptchaProducer) {
        this.kaptchaProducer = kaptchaProducer;
    }

    @Autowired
    public void setRedisTemplate(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }


    @RequestMapping(path = "/login", method = RequestMethod.GET)
    public String getLoginPage() {
        return "/site/login";
    }

    // ??????????????????session????????????redis??????????????????????????????????????????????????????session????????????
    @RequestMapping(path = "/kaptcha", method = RequestMethod.GET)
    public void getKaptcha(HttpServletResponse response/*, HttpSession session*/) {
        // ???????????????
        String text = kaptchaProducer.createText();
        BufferedImage image = kaptchaProducer.createImage(text);

        // ??????????????????session
//        session.setAttribute("kaptcha", text);

        // ???????????????
        String kaptchaOwner = CommunityUtil.generateUUID();
        Cookie cookie = new Cookie("kaptchaOwner", kaptchaOwner);
        cookie.setPath(contextPath);
        cookie.setMaxAge(60);
        response.addCookie(cookie);
        // ??????????????????redis?????????
        String kaptchaKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
        redisTemplate.opsForValue().set(kaptchaKey, text, 60, TimeUnit.SECONDS);


        // ????????????????????????
        response.setContentType("image/png");
        try {
            ServletOutputStream outputStream = response.getOutputStream();
            ImageIO.write(image, "png", outputStream);
        } catch (IOException e) {
            logger.error("?????????????????????:" + e.getMessage());
        }
    }

    @RequestMapping(path = "/login", method = RequestMethod.POST)
    public ModelAndView login(String username, String password, String code, boolean remember,
                              HttpServletResponse response, HttpServletRequest request/*, HttpSession session*/) {
        ModelAndView mv = new ModelAndView();
        // 1. ???????????????????????????
        String kaptcha = null;
        String kaptchaOwner = CookieUtil.getValue(request, "kaptchaOwner");
        // ??????kaptchaOwner????????????
        if (!StringUtils.isBlank(kaptchaOwner)) {
            // ???redis???????????????????????????
            String kaptchaKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
            kaptcha = (String) redisTemplate.opsForValue().get(kaptchaKey);
        }

        if (kaptcha == null || !kaptcha.equalsIgnoreCase(code)) {
            if (kaptcha == null) mv.addObject("codeMsg", "???????????????!");
            else  mv.addObject("codeMsg", "???????????????!");
            mv.addObject("username", username);
            mv.addObject("password", password);
            mv.setViewName("/site/login");
            return mv;
        }
        // 2. ??????UserService???login??????
        // ????????????remember?????????
        int expiredSeconds = remember? REMEMBER_EXPIRED_SECONDS : DEFAULT_EXPIRED_SECONDS;
        Map<String, String> map = userService.login(username, password, expiredSeconds);
        if (map.containsKey("userMsg")) {
            mv.addObject("userMsg", map.get("userMsg"));
            mv.addObject("username", username);
            mv.addObject("password", password);
            mv.setViewName("/site/login");
            return mv;
        }
        if (map.containsKey("pwdMsg")) {
            mv.addObject("pwdMsg", map.get("pwdMsg"));
            mv.addObject("username", username);
            mv.addObject("password", password);
            mv.setViewName("/site/login");
            return mv;
        }
        String ticket = map.get("ticket");
        Cookie cookie = new Cookie("ticket", ticket);
        cookie.setMaxAge(expiredSeconds);
        cookie.setPath(contextPath);
        response.addCookie(cookie);
        mv.setViewName("redirect:/index");
        return mv;
    }

    @RequestMapping(path = "/logout", method = RequestMethod.GET)
    public String logout(@CookieValue("ticket") String ticket, HttpServletResponse response) {
        // ??????userService???logout??????
        userService.logout(ticket);
        // ???SpringSecurity??????????????????token??????
        SecurityContextHolder.clearContext();
        return "redirect:/login";
    }
}
