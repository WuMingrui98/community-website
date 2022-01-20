package com.wmr.community.controller;

import com.wmr.community.entity.DiscussPost;
import com.wmr.community.entity.Page;
import com.wmr.community.service.ElasticsearchService;
import com.wmr.community.service.LikeService;
import com.wmr.community.service.UserService;
import com.wmr.community.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class SearchController implements CommunityConstant {
    private ElasticsearchService elasticsearchService;

    private UserService userService;

    private LikeService likeService;

    @Autowired
    public void setElasticsearchService(ElasticsearchService elasticsearchService) {
        this.elasticsearchService = elasticsearchService;
    }

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    public void setLikeService(LikeService likeService) {
        this.likeService = likeService;
    }

    @GetMapping(path = "/search")
    public ModelAndView search(@RequestParam(value = "keyword") String keyword, Page page) {
        ModelAndView mv = new ModelAndView();
        // 搜索帖子
        SearchHits<DiscussPost> search = elasticsearchService.searchDiscussPost(keyword, page.getCurrent() - 1, page.getLimit());
        // 聚合数据
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        List<SearchHit<DiscussPost>> searchHits = search.getSearchHits();
        for (SearchHit<DiscussPost> searchHit : searchHits) {
            Map<String, Object> map = new HashMap<>();
            DiscussPost post = searchHit.getContent();
            // 帖子
            map.put("post", post);
            // 作者
            map.put("user", userService.findUserById(post.getUserId()));
            // 点赞数量
            map.put("likeCount", likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId()));
            discussPosts.add(map);
        }
        mv.addObject("discussPosts", discussPosts);

        // 分页设置
        page.setPath("/search?keyword=" + keyword);
        page.setRows((int) search.getTotalHits());
        mv.addObject("page", page);
        mv.addObject("keyword", keyword);

        mv.setViewName("/site/search");
        return mv;
    }
}
