package com.wmr.community.controller;

import com.wmr.community.entity.User;
import com.wmr.community.service.UserService;
import com.wmr.community.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

@Controller
public class RegisterController implements CommunityConstant {
    private UserService userService;

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @RequestMapping(path = "/register", method = RequestMethod.GET)
    public String getRegisterPage() {
        return "/site/register";
    }

    @RequestMapping(path = "/register", method = RequestMethod.POST)
    public ModelAndView register(User user) {
        ModelAndView mv = new ModelAndView();
        Map<String, String> map = userService.register(user);
        if (map != null) {
            String userMsg = map.get("userMsg");
            String emailMsg = map.get("emailMsg");
            mv.addObject("userMsg", userMsg);
            mv.addObject("emailMsg", emailMsg);
            mv.addObject("user", user);
            mv.setViewName("/site/register");
        } else {
            String msg = "注册成功,我们已经向您的邮箱发送了一封激活邮件,请尽快激活!";
            mv.addObject("msg", msg);
            mv.addObject("target", "/index");
            mv.setViewName("/site/operate-result");
        }
        return mv;
    }

    @RequestMapping(path = "/activation/{id}/{activationCode}", method = RequestMethod.GET)
    public ModelAndView activation(
            @PathVariable(name = "id") int id,
            @PathVariable(name = "activationCode") String activationCode) {
        ModelAndView mv = new ModelAndView();
        int result = userService.activation(id, activationCode);
        if (result == ACTIVATION_SUCCESS) {
            mv.addObject("msg", "激活成功,您的账号已经可以正常使用了!");
            mv.addObject("target", "/login");
        } else if (result == ACTIVATION_REPEAT) {
            mv.addObject("msg", "无效操作,该账号已经激活过了!");
            mv.addObject("target", "/index");
        } else {
            mv.addObject("msg", "激活失败,您提供的激活码不正确!");
            mv.addObject("target", "/index");
        }
        mv.setViewName("/site/operate-result");
        return mv;
    }


}
