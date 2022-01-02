package com.wmr.community.service;

import com.wmr.community.dao.AlphaDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Service
// 默认单例
@Scope("prototype")
public class AlphaService {

    // 在通过属性注入
//    @Autowired
//    @Qualifier("alphaDAOHibernate")
    private AlphaDAO alphaDAO;

    @Autowired
    @Qualifier("alphaDAOHibernate")
    public void setAlphaDAO(AlphaDAO alphaDAO) {
        this.alphaDAO = alphaDAO;
    }

//    public AlphaService() {
//        System.out.println("实例化AlphaService");
//    }
//
//    @PostConstruct
//    public void init() {
//        System.out.println("初始化AlphaService");
//    }
//
//    @PreDestroy
//    public void destroy() {
//        System.out.println("销毁AlphaService");
//    }

    public String select() {
        return alphaDAO.select();
    }
}
