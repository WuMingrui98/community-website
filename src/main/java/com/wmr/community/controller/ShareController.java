package com.wmr.community.controller;

import com.wmr.community.entity.Event;
import com.wmr.community.event.EventProducer;
import com.wmr.community.util.CommunityConstant;
import com.wmr.community.util.CommunityUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Controller
public class ShareController implements CommunityConstant {
    private static final Logger logger = LoggerFactory.getLogger(ShareController.class);

    private EventProducer eventProducer;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${wk.image.storage}")
    private String wkImageStorage;

    @Value("${qiniu.bucket.share.url}")
    private String shareBucketUrl;

    @Autowired
    public void setEventProducer(EventProducer eventProducer) {
        this.eventProducer = eventProducer;
    }


    @GetMapping(path = "/share")
    @ResponseBody
    public String share(String htmlUrl) throws IOException {
        // 文件名
        String filename = CommunityUtil.generateUUID();

        // 利用消息队列发送事件异步生成长图
        Event event = new Event()
                .setTopic(TOPIC_SHARE)
                .setData("htmlUrl", htmlUrl)
                .setData("filename", filename)
                .setData("suffix", ".png");
        eventProducer.fireEvent(event);

        // 返回访问路径
        Map<String, Object> map = new HashMap<>();
//        map.put("shareUrl", domain + contextPath + "/share/image/" + filename);
        map.put("shareUrl", shareBucketUrl + "/" + filename);

        return CommunityUtil.getJSONString(0, null, map);
    }


    // 获取长图
    @Deprecated
    @GetMapping(path = "/share/image/{filename}")
    public void getShareImage(@PathVariable("filename") String filename, HttpServletResponse response) {
        if (StringUtils.isBlank(filename)) {
            throw new IllegalArgumentException("文件名不能为空!");
        }
        response.setContentType("/image/png");
        File file = new File(wkImageStorage + "/" + filename + ".png");
        try {
            ServletOutputStream outputStream = response.getOutputStream();
            FileInputStream fileInputStream = new FileInputStream(file);
            byte[] buff = new byte[1024];
            int len = 0;
            while ((len = fileInputStream.read(buff)) != -1) {
                outputStream.write(buff, 0, len);
            }
        } catch (IOException e) {
            logger.error("获取长图失败: " + e.getMessage());
        }

    }
}
