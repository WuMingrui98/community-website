package com.wmr.community;

import com.wmr.community.dao.DiscussPostMapper;
import com.wmr.community.dao.LoginTicketMapper;
import com.wmr.community.dao.UserMapper;
import com.wmr.community.entity.DiscussPost;
import com.wmr.community.entity.LoginTicket;
import com.wmr.community.entity.User;
import com.wmr.community.util.CommunityUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.sql.Time;
import java.util.Date;
import java.util.List;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MapperTest {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private DiscussPostMapper discussPostMapper;
    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Test
    public void testInsertLoginTicket() {
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setUserId(151);
        loginTicket.setExpired(new Date());
        loginTicket.setStatus(0);
        loginTicketMapper.insertLoginTicket(loginTicket);
    }

    @Test
    public void testSelectByTicket() {
        String ticket = "8b9b6b2729544be9af79be49c5732596";
        LoginTicket loginTicket = loginTicketMapper.selectByTicket(ticket);
        System.out.println(loginTicket);
    }

    @Test
    public void updateStatus() {
        String ticket = "8b9b6b2729544be9af79be49c5732596";
        int i = loginTicketMapper.updateStatus(ticket, 1);
        System.out.println(i);
    }

    @Test
    public void DiscussPostMapper() {
        int i = discussPostMapper.selectDiscussPostRows(0);
        System.out.println(i);
        List<DiscussPost> discussPosts = discussPostMapper.selectDiscussPosts(0, 0, 10);
        System.out.println(discussPosts);
    }

    @Test
    public void testSelectUser() {
        User user = userMapper.selectById(101);
        System.out.println(user);

        user = userMapper.selectByName("wmrr22");
        System.out.println(user);

        user = userMapper.selectByEmail("nowcoder101@sina.com");
        System.out.println(user);
    }

    @Test
    public void testInsertUser() {
        User user = new User();
        user.setUsername("test");
        user.setPassword("123456");
        user.setSalt("abc");
        user.setEmail("test@qq.com");
        user.setHeaderUrl("http://www.nowcoder.com/101.png");
        user.setCreateTime(new Date());

        int rows = userMapper.insertUser(user);
        System.out.println(rows);
        System.out.println(user.getId());
    }

    @Test
    public void updateUser() {
        int rows = userMapper.updateStatus(150, 1);
        System.out.println(rows);

        rows = userMapper.updateHeader(150, "http://www.nowcoder.com/102.png");
        System.out.println(rows);

        rows = userMapper.updatePassword(150, "hello");
        System.out.println(rows);
    }


}
