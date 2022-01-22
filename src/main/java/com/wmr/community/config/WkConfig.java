package com.wmr.community.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.File;

@Configuration
public class WkConfig {
    private static final Logger logger = LoggerFactory.getLogger(WkConfig.class);

    @Value("${wk.image.storage}")
    private String wkImageStorage;

    @PostConstruct
    public void init() {
        // 创建wk图片目录
        File file = new File(wkImageStorage);
        if (!file.exists()){
            if (!file.mkdirs()) {
                logger.error("创建wk图片目录失败!");
                throw new RuntimeException("创建wk图片目录失败!");
            }
            logger.info("创建WK图片目录: " + wkImageStorage);
        }
    }
}
