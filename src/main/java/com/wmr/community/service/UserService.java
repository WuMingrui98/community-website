package com.wmr.community.service;

import com.wmr.community.dao.LoginTicketMapper;
import com.wmr.community.dao.UserMapper;
import com.wmr.community.entity.LoginTicket;
import com.wmr.community.entity.User;
import com.wmr.community.util.CommunityConstant;
import com.wmr.community.util.CommunityUtil;
import com.wmr.community.util.MailClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class UserService implements CommunityConstant {

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    private UserMapper userMapper;

    private LoginTicketMapper loginTicketMapper;

    private MailClient mailClient;

    private TemplateEngine templateEngine;

    @Autowired
    public void setLoginTicketMapper(LoginTicketMapper loginTicketMapper) {
        this.loginTicketMapper = loginTicketMapper;
    }

    @Autowired
    public void setTemplateEngine(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    @Autowired
    public void setMailClient(MailClient mailClient) {
        this.mailClient = mailClient;
    }

    @Autowired
    public void setUserMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public User findUserById(int id) {
        return userMapper.selectById(id);
    }

    /**
     * 完成注册功能，实现以下小的功能模块
     * 1、判断用户信息是否合法，用户名和邮箱是否已经注册
     * 2、完善自动完善用户信息后，将数据填入数据库
     * 3、发送注册邮件给用户
     *
     * @param user 从表现层传入的初步封装值的User对象
     * @return 返回map，map中封装有和用户名和邮箱是否已经存在相关的消息
     */
    public Map<String, String> register(User user) {
        Map<String, String> map = new HashMap<>();
        String username = user.getUsername();
        if (userMapper.selectByName(username) != null) {
            map.put("userMsg", "该用户已经存在！");
        }
        String email = user.getEmail();
        if (userMapper.selectByEmail(email) != null) {
            map.put("emailMsg", "该邮箱已经注册！");
        }
        if (!map.isEmpty()) return map;
        // 进一步完善用户的信息，注册用户
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setActivationCode(CommunityUtil.generateUUID());
        // 未激活状态
        user.setStatus(0);
        user.setType(0);
        user.setCreateTime(new Date());

        // 将用户数据添加数据库
        userMapper.insertUser(user);

        // 发送邮件
        String link = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        context.setVariable("link", link);
        String content = templateEngine.process("/mail/activation", context);
        mailClient.sendHtmlMail(user.getEmail(), "激活账号", content);
        return null;
    }

    /**
     * 完成激活的功能，实现以下小的功能模块
     * 1. 判断激活码和用户id是否能够对应
     * 2. 如果判断对应，则修改数据库中用户的status:0->1
     *
     * @param id 用户id
     * @param activationCode 用户激活码
     * @return 返回激活信息(0: 激活成功, 1: 重复激活, 2: 激活失败)
     */
    public int activation(int id, String activationCode) {
        User user = userMapper.selectById(id);
        // 判断用户是否存在
        if (user != null) {
            // 判断激活码是否对应
            if (user.getActivationCode().equals(activationCode)) {
                if (user.getStatus() == 0) {
                    // 将数据库中用户的激活状态设置为1
                    userMapper.updateStatus(id, 1);
                    return ACTIVATION_SUCCESS;
                } else {
                    return ACTIVATION_REPEAT;
                }
            }
        }
        return ACTIVATION_FAILURE;
    }


    /**
     * 完成登录的功能，实现以下小的功能模块
     * 1. 判断用户是否存在
     * 2. 判断用户是否激活
     * 3. 判断密码是否正确（要和salt结合）
     * 4. 将登录信息保存到login_ticket表
     * 5. 将登录凭证传给表现层
     *
     * @param username 用户名
     * @param password 密码
     * @param expiredSeconds 凭证过期时间
     * @return 返回map，map中封装有错误及凭证相关的消息
     */
    public Map<String, String> login(String username, String password, int expiredSeconds) {
        Map<String, String> map = new HashMap<>();
        // 1. 判断用户是否存在
        User user = userMapper.selectByName(username);
        if (user == null) {
            map.put("userMsg", "用户不存在!");
            return map;
        }
        // 2. 判断用户是否激活
        if (user.getStatus() == 0) {
            map.put("userMsg", "用户未激活!");
            return map;
        }
        // 3. 判断密码是否正确（要和salt结合）
        if (!user.getPassword().equals(CommunityUtil.md5(password + user.getSalt()))) {
            map.put("pwdMsg", "密码错误!");
            return map;
        }
        // 4. 将登录信息保存到login_ticket表
        LoginTicket loginTicket = new LoginTicket();
        String ticket = CommunityUtil.generateUUID();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(ticket);
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000L));
        loginTicketMapper.insertLoginTicket(loginTicket);
        // 5. 将登录凭证传给表现层
        map.put("ticket", ticket);
        return map;
    }

    /**
     * 完成退出登录的功能，实现以下小的功能模块
     * 1. 根据ticket将login_ticket表中对应的status从0->1
     *
     */
    public void logout(String ticket) {
        loginTicketMapper.updateStatus(ticket, 1);
    }

    /**
     * 通过登录凭证获取到当前登录的用户
     * @param ticket 登录凭证
     * @return 返回通过登录凭证查询到的用户，如果凭证无效或者过期返回null
     */
    public User findUserByTicket(String ticket) {
        LoginTicket loginTicket = loginTicketMapper.selectByTicket(ticket);
        if (loginTicket == null || loginTicket.getStatus() == 1 || !loginTicket.getExpired().after(new Date())) {
            return null;
        }
        int userId = loginTicket.getUserId();
        return userMapper.selectById(userId);
    }

    /**
     * 更新用户的头像链接
     * @return 返回1成功，返回0失败
     */
    public int updateHeader(int id, String headerUrl) {
        return userMapper.updateHeader(id, headerUrl);
    }

    /**
     * 完成更新密码的功能，实现以下小的功能模块
     * 1. 查询用户密码，并将输入的原密码与用户密码比较，如果不一致，则返回错误信息
     * 2. 输入密码与用户原密码一致，则通过持久层更新用户的密码
     *
     * @param id 用户id
     * @param oldPassword 原密码
     * @param newPassword 新密码
     * @return 返回错误信息，如果没有出错，则返回null
     */
    public String updatePassword(int id, String oldPassword, String newPassword) {
        // 1. 查询用户密码，并将输入的原密码与用户密码比较，如果不一致，则返回错误信息
        User user = userMapper.selectById(id);
        if (!user.getPassword().equals(CommunityUtil.md5(oldPassword + user.getSalt()))) {
            return "密码错误，请重新输入!";
        }
        // 2. 输入密码与用户原密码一致，则通过持久层更新用户的密码
        userMapper.updatePassword(id, CommunityUtil.md5(newPassword + user.getSalt()));
        return null;
    }

}
