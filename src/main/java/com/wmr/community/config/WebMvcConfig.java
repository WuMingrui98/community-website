package com.wmr.community.config;

import com.wmr.community.controller.interceptor.*;
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

    private DataInterceptor dataInterceptor;

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

    @Autowired
    public void setDataInterceptor(DataInterceptor dataInterceptor) {
        this.dataInterceptor = dataInterceptor;
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

        registry.addInterceptor(dataInterceptor)
                .excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.png", "/**/*.jpg", "/**/*.jpeg");
    }
}
