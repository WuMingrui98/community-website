package com.wmr.community.controller;

import com.wmr.community.entity.DiscussPost;
import com.wmr.community.entity.User;
import com.wmr.community.service.DiscussPostService;
import com.wmr.community.util.CommunityUtil;
import com.wmr.community.util.HostHolder;
import com.wmr.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController {

    private DiscussPostService discussPostService;

    private HostHolder hostHolder;

    @Autowired
    public void setDiscussPostService(DiscussPostService discussPostService) {
        this.discussPostService = discussPostService;
    }

    @Autowired
    public void setHostHolder(HostHolder hostHolder) {
        this.hostHolder = hostHolder;
    }

    @RequestMapping(path = "/add", method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost(
            @RequestParam(name = "title") String title,
            @RequestParam(name = "content") String content
    ) {
        User user = hostHolder.getUser();
        DiscussPost discussPost = new DiscussPost();
        discussPost.setUserId(user.getId());
        discussPost.setContent(content);
        discussPost.setTitle(title);
        discussPost.setCreateTime(new Date());
        discussPostService.addDiscussPost(discussPost);
        // 报错的情况,将来统一处理，先假定业务逻辑没有问题
        return CommunityUtil.getJSONString(0, "发布成功");
    }
}
