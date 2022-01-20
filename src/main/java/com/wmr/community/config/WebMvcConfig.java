package com.wmr.community.config;

import com.wmr.community.controller.interceptor.AlphaInterceptor;
import com.wmr.community.controller.interceptor.LoginRequiredInterceptor;
import com.wmr.community.controller.interceptor.LoginTicketInterceptor;
import com.wmr.community.controller.interceptor.MessageInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
//    private AlphaInterceptor alphaInterceptor;

    private LoginTicketInterceptor loginTicketInterceptor;

//    private LoginRequiredInterceptor loginRequiredInterceptor;

    private MessageInterceptor messageInterceptor;

//    @Autowired
//    public void setAlphaInterceptor(AlphaInterceptor alphaInterceptor) {
//        this.alphaInterceptor = alphaInterceptor;
//    }

    @Autowired
    public void setLoginTicketInterceptor(LoginTicketInterceptor loginTicketInterceptor) {
        this.loginTicketInterceptor = loginTicketInterceptor;
    }

//    @Autowired
//    public void setLoginRequiredInterceptor(LoginRequiredInterceptor loginRequiredInterceptor) {
//        this.loginRequiredInterceptor = loginRequiredInterceptor;
//    }

    @Autowired
    public void setMessageInterceptor(MessageInterceptor messageInterceptor) {
        this.messageInterceptor = messageInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
//        registry.addInterceptor(alphaInterceptor)
//                .excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.png", "/**/*.jpg", "/**/*.jpeg")
//                .addPathPatterns("/register", "/login");

        registry.addInterceptor(loginTicketInterceptor)
                .excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.png", "/**/*.jpg", "/**/*.jpeg");

//        registry.addInterceptor(loginRequiredInterceptor)
//                .excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.png", "/**/*.jpg", "/**/*.jpeg");

        registry.addInterceptor(messageInterceptor)
                .excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.png", "/**/*.jpg", "/**/*.jpeg");

    }
}
