package com.wmr.community.controller;

import com.wmr.community.entity.DiscussPost;
import com.wmr.community.entity.User;
import com.wmr.community.service.DiscussPostService;
import com.wmr.community.service.UserService;
import com.wmr.community.util.CommunityUtil;
import com.wmr.community.util.HostHolder;
import com.wmr.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.Date;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController {

    private DiscussPostService discussPostService;

    private UserService userService;

    private HostHolder hostHolder;

    @Autowired
    public void setDiscussPostService(DiscussPostService discussPostService) {
        this.discussPostService = discussPostService;
    }

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
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

    @RequestMapping(path = "/detail/{id}", method = RequestMethod.GET)
    public ModelAndView showPostDetail(
            @PathVariable(name = "id") int id
    ) {
        ModelAndView mv = new ModelAndView();
        DiscussPost discussPost = discussPostService.findDiscussPostById(id);
        User user = null;
        if (discussPost != null) {
            user = userService.findUserById(discussPost.getUserId());
        }
        mv.addObject("post", discussPost);
        mv.addObject("user", user);
        if (discussPost == null || user == null) {
            mv.setViewName("redirect:/index");
            return mv;
        }
        mv.setViewName("/site/discuss-detail");
        return mv;
    }
}
