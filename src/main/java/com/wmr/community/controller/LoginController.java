package com.wmr.community.controller;

import com.google.code.kaptcha.Producer;
import com.wmr.community.service.UserService;
import com.wmr.community.util.CommunityConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;

@Controller
public class LoginController implements CommunityConstant {
    private final Logger logger = LoggerFactory.getLogger(LoginController.class);
    private UserService userService;

    private Producer kaptchaProducer;

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



    @RequestMapping(path = "/login", method = RequestMethod.GET)
    public String getLoginPage() {
        return "/site/login";
    }

    @RequestMapping(path = "/kaptcha", method = RequestMethod.GET)
    public void getKaptcha(HttpServletResponse response, HttpSession session) {
        // 生成验证码
        String text = kaptchaProducer.createText();
        BufferedImage image = kaptchaProducer.createImage(text);

        // 将验证码存入session
        session.setAttribute("kaptcha", text);

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
                              HttpServletResponse response, HttpSession session) {
        ModelAndView mv = new ModelAndView();
        // 1. 判断验证码是否正确
        if (!((String) session.getAttribute("kaptcha")).equalsIgnoreCase(code)) {
            mv.addObject("codeMsg", "验证码错误!");
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
        return "redirect:/login";
    }
}
