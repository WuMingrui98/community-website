package com.wmr.community.service;

import com.wmr.community.dao.AlphaDAO;
import com.wmr.community.dao.DiscussPostMapper;
import com.wmr.community.dao.UserMapper;
import com.wmr.community.entity.DiscussPost;
import com.wmr.community.entity.User;
import com.wmr.community.util.CommunityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Date;

@Service
// 默认单例
@Scope("prototype")
public class AlphaService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private TransactionTemplate transactionTemplate;

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

    // 事务管理学习

    // 1.声明式事务
    // - 通过XML配置，声明某方法的事务特征。 - 通过注解，声明某方法的事务特征。

    // REQUIRED: 支持当前事务（外部事务），如果不存在则创建新事务
    // REQUIRED_NEW：创建一个新水哦无，并且暂停当前事务（外部事务）
    // NESTED: 如果当前存在事务（外部事务），则嵌套在该事务中执行（独立的提交和回滚），否则会和REQUIRED一样
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public Object save1() {
        // 新增用户
        User user = new User();
        user.setUsername("alpha");
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
        user.setPassword(CommunityUtil.md5("123" + user.getSalt()));
        user.setEmail("alpha@qq.com");
        user.setHeaderUrl("http://image.nowcoder.com/head/99t.png");
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        // 新增帖子
        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle("Hello");
        post.setContent("新人报道!");
        post.setCreateTime(new Date());
        discussPostMapper.insertDiscussPost(post);

        Integer.valueOf("abc");
        return "ok";
    }

    // 2.编程式事务
    // - 通过 TransactionTemplate 管理事务，并通过它执行数据库的操作。
    public Object save2() {
        transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        // transactionTemplate执行回调函数
        return transactionTemplate.execute(new TransactionCallback<Object>() {

            @Override
            public Object doInTransaction(TransactionStatus status) {
                // 新增用户
                User user = new User();
                user.setUsername("alpha");
                user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
                user.setPassword(CommunityUtil.md5("123" + user.getSalt()));
                user.setEmail("alpha@qq.com");
                user.setHeaderUrl("http://image.nowcoder.com/head/99t.png");
                user.setCreateTime(new Date());
                userMapper.insertUser(user);

                // 新增帖子
                DiscussPost post = new DiscussPost();
                post.setUserId(user.getId());
                post.setTitle("Hello");
                post.setContent("新人报道!");
                post.setCreateTime(new Date());
                discussPostMapper.insertDiscussPost(post);

                Integer.valueOf("abc");
                return "ok";
            }
        });
    }
}
