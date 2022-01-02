package com.wmr.community;

import com.wmr.community.util.MailClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MailClientTest {
    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Test
    public void testSendMail() {
//        String content = "你好啊，兄弟";
        String subject = "hello";
        String to = "771478063@qq.com";
//        mailClient.sendSimpleMail(to, subject, content);

        Context context = new Context();
        context.setVariable("username", "WMR");
        String content = templateEngine.process("/mail/demo", context);
        System.out.println(content);
        mailClient.sendHtmlMail(to, subject, content);
    }
}
