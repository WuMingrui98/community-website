package com.wmr.community;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.io.IOException;

public class WkTest {
    public static void main(String[] args) {
        String cmd = "/usr/local/bin/wkhtmltoimage --quality 75 https://www.baidu.com /Users/mingruiwu/Documents/community/wk-images/1.png";
        try {
            Runtime.getRuntime().exec(cmd);
            Thread.sleep(10 * 1000);
            System.out.println("ok");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
