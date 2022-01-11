package com.wmr.community.controller;

import com.wmr.community.dao.DiscussPostMapper;
import com.wmr.community.entity.DiscussPost;
import com.wmr.community.entity.Page;
import com.wmr.community.entity.User;
import com.wmr.community.service.DiscussPostService;
import com.wmr.community.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {

    private UserService userService;
    private DiscussPostService discussPostService;

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    public void setDiscussPostService(DiscussPostService discussPostService) {
        this.discussPostService = discussPostService;
    }

    @RequestMapping(path = "/index", method = RequestMethod.GET)
    public ModelAndView getIndexPage(Page page,
                                     @RequestParam(name = "current", required = false, defaultValue = "1") int current) {
        page.setCurrent(current);
        page.setPath("/index");
        page.setRows(discussPostService.findDiscussPostRows(0));
        List<DiscussPost> list = discussPostService.findDiscussPosts(0, page.getOffset(), 10);
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if (list != null) {
            for(DiscussPost discussPost : list) {
                Map<String, Object> map = new HashMap<>();
                User user = userService.findUserById(discussPost.getUserId());
                map.put("user", user);
                map.put("post", discussPost);
                discussPosts.add(map);
            }
        }
        ModelAndView mv = new ModelAndView();
        mv.addObject("page", page);
        mv.addObject("discussPosts", discussPosts);
        mv.setViewName("/index");
        return mv;
    }


    @RequestMapping(path = "/error", method = RequestMethod.GET)
    public String getErrorPage() {
        return "/error/500";
    }
}
