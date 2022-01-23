package com.wmr.community;

import com.wmr.community.entity.DiscussPost;
import com.wmr.community.service.DiscussPostService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;

@SpringBootTest
public class SpringBootTests {
    @Autowired
    private DiscussPostService discussPostService;

    private DiscussPost data;

    // BeforeAll和AfterAll在所有方法之前和之后各执行一次，在静态方法上使用
    // BeforeEach和AfterEach在每个方法之前和之后各执行一次，在普通方法上使用
    @BeforeAll
    public static void beforeAll() {
        System.out.println("beforeAll");
    }

    @AfterAll
    public static void afterAll() {
        System.out.println("afterAll");
    }

    @BeforeEach
    public void beforeEach() {
        System.out.println("beforeEach");
        // 初始化测试数据
        data = new DiscussPost();
        data.setUserId(111);
        data.setTitle("Test Title");
        data.setContent("Test Content");
        data.setCreateTime(new Date());
        discussPostService.addDiscussPost(data);
    }

    @AfterEach
    public void afterEach() {
        System.out.println("afterEach");
        // 删除测试数据
        discussPostService.updateStatus(data.getId(), 2);
    }

    @Test
    public void test1() {
        System.out.println("test1");
    }

    @Test
    public void test2() {
        System.out.println("test2");
    }

    @Test
    public void testFindById() {
        DiscussPost post = discussPostService.findDiscussPostById(data.getId());
        Assertions.assertNotNull(post);
        Assertions.assertEquals(data.getTitle(), post.getTitle());
        Assertions.assertEquals(data.getContent(), post.getContent());
    }

    @Test
    public void testUpdateScore() {
        int rows = discussPostService.updateScore(data.getId(), 2000.00);
        Assertions.assertEquals(1, rows);

        DiscussPost post = discussPostService.findDiscussPostById(data.getId());
        Assertions.assertEquals(post.getScore(), 2000, 2);
    }
}
