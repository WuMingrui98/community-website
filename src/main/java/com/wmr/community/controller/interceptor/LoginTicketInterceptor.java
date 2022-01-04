package com.wmr.community.controller.interceptor;

import com.wmr.community.entity.User;
import com.wmr.community.service.UserService;
import com.wmr.community.util.CookieUtil;
import com.wmr.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class LoginTicketInterceptor implements HandlerInterceptor {
    private HostHolder hostHolder;

    private UserService userService;

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    public void setHostHolder(HostHolder hostHolder) {
        this.hostHolder = hostHolder;
    }

    // 在Controller执行之前，通过ticket查询一下对应的User，并存到hostHolder中
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String ticket = CookieUtil.getValue(request, "ticket");
        User user = userService.getUserByTicket(ticket);
        hostHolder.setUser(user);
        return true;
    }

    // 在Controller执行之后，通过hostHolder取出User对象，并存入到ModelAndView对象中
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user = hostHolder.getUser();
        if (user != null && modelAndView != null) {
            modelAndView.addObject("loginUser", user);
        }
    }

    // 在TemplateEngine之后执行, 对hostHolder中的ThreadLocal对象进行清理
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        hostHolder.clear();
    }
}
