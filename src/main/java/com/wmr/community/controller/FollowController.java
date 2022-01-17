package com.wmr.community.controller;

import com.wmr.community.entity.Page;
import com.wmr.community.entity.User;
import com.wmr.community.service.FollowService;
import com.wmr.community.service.UserService;
import com.wmr.community.util.CommunityConstant;
import com.wmr.community.util.CommunityUtil;
import com.wmr.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class FollowController implements CommunityConstant {
    private FollowService followService;
    private UserService userService;
    private HostHolder hostHolder;

    @Autowired
    public void setFollowService(FollowService followService) {
        this.followService = followService;
    }

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    public void setHostHolder(HostHolder hostHolder) {
        this.hostHolder = hostHolder;
    }

    @RequestMapping(path = "/follow", method = RequestMethod.POST)
    @ResponseBody
    public String follow(int entityType, int entityId) {
        User user = hostHolder.getUser();
        if (user == null) {
            return CommunityUtil.getJSONString(1, "未登录，不能关注!");
        }
        followService.follow(user.getId(), entityType, entityId);
        return CommunityUtil.getJSONString(0, "已关注!");

    }


    @RequestMapping(path = "/unfollow", method = RequestMethod.POST)
    @ResponseBody
    public String unfollow(int entityType, int entityId) {
        User user = hostHolder.getUser();
        followService.unfollow(user.getId(), entityType, entityId);
        return CommunityUtil.getJSONString(0, "已取消关注!");
    }


    @RequestMapping(path = "/followees/{userId}", method = RequestMethod.GET)
    public ModelAndView getFollowees(@PathVariable("userId") int userId, Page page) {
        ModelAndView mv = new ModelAndView();
        // 当前查看的用户
        User user = userService.findUserById(userId);
        if (user == null) throw new RuntimeException("该用户不存在!");

        mv.addObject("user", user);

        // 处理分页
        page.setPath("/followees");
        page.setRows((int) followService.findFolloweeCount(userId, ENTITY_TYPE_USER));

        List<Map<String, Object>> followees = followService.findFollowees(userId, page.getOffset(), page.getLimit());
        if (followees != null) {
            for (Map<String, Object> map : followees) {
                User followee = (User) map.get("followee");
                map.put("hasFollowed", hasFollowed(followee.getId()));
            }
        }
        mv.addObject("followees", followees);
        mv.setViewName("/site/followee");
        return mv;
    }


    @RequestMapping(path = "followers/{userId}", method = RequestMethod.GET)
    public ModelAndView getFollowers(@PathVariable("userId") int userId, Page page) {
        ModelAndView mv = new ModelAndView();
        // 当前查看的用户
        User user = userService.findUserById(userId);
        if (user == null) throw new RuntimeException("该用户不存在!");

        mv.addObject("user", user);

        // 处理分页
        page.setPath("/followers");
        page.setRows((int) followService.findFollowerCount(userId, ENTITY_TYPE_USER));

        List<Map<String, Object>> followers = followService.findFollowers(userId, page.getOffset(), page.getLimit());
        if (followers != null) {
            for (Map<String, Object> map : followers) {
                User follower = (User) map.get("follower");
                map.put("hasFollowed", hasFollowed(follower.getId()));
            }
        }
        mv.addObject("followers", followers);
        mv.setViewName("/site/follower");
        return mv;
    }

    private boolean hasFollowed(int userId) {
        if (hostHolder.getUser() == null) {
            return false;
        }
        return followService.hasFollowed(hostHolder.getUser().getId(), CommunityConstant.ENTITY_TYPE_USER, userId);
    }
}
