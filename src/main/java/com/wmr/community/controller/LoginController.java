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

    // 验证码不存在session中，存在redis数据库中，也可以解决分布式部署存在的session共享问题
    @RequestMapping(path = "/kaptcha", method = RequestMethod.GET)
    public void getKaptcha(HttpServletResponse response/*, HttpSession session*/) {
        // 生成验证码
        String text = kaptchaProducer.createText();
        BufferedImage image = kaptchaProducer.createImage(text);

        // 将验证码存入session
//        session.setAttribute("kaptcha", text);

        // 验证码归属
        String kaptchaOwner = CommunityUtil.generateUUID();
        Cookie cookie = new Cookie("kaptchaOwner", kaptchaOwner);
        cookie.setPath(contextPath);
        cookie.setMaxAge(60);
        response.addCookie(cookie);
        // 将验证码存到redis数据库
        String kaptchaKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
        redisTemplate.opsForValue().set(kaptchaKey, text, 60, TimeUnit.SECONDS);


        // 将图片发给浏览器
        response.setContentType("image/png");
        try {
            ServletOutputStream outputStream = response.getOutputStream();
            ImageIO.write(image, "png", outputStream);
        } catch (IOException e) {
            logger.error("响应验证码失败:" + e.getMessage());
        }
    }

    @RequestMapping(path = "/login", method = RequestMethod.POST)
    public ModelAndView login(String username, String password, String code, boolean remember,
                              HttpServletResponse response, HttpServletRequest request/*, HttpSession session*/) {
        ModelAndView mv = new ModelAndView();
        // 1. 判断验证码是否正确
        String kaptcha = null;
        String kaptchaOwner = CookieUtil.getValue(request, "kaptchaOwner");
        // 判断kaptchaOwner是否为空
        if (!StringUtils.isBlank(kaptchaOwner)) {
            // 从redis数据库中取出验证码
            String kaptchaKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
            kaptcha = (String) redisTemplate.opsForValue().get(kaptchaKey);
        }

        if (kaptcha == null || !kaptcha.equalsIgnoreCase(code)) {
            if (kaptcha == null) mv.addObject("codeMsg", "验证码过期!");
            else  mv.addObject("codeMsg", "验证码错误!");
            mv.addObject("username", username);
            mv.addObject("password", password);
            mv.setViewName("/site/login");
            return mv;
        }
        // 2. 调用UserService的login服务
        // 判断一下remember的状态
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
        // 调用userService的logout服务
        userService.logout(ticket);
        // 将SpringSecurity中存储的凭证token清除
        SecurityContextHolder.clearContext();
        return "redirect:/login";
    }
}
