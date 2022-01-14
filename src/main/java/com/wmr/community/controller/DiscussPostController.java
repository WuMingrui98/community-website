package com.wmr.community.controller;

import com.wmr.community.entity.Comment;
import com.wmr.community.entity.DiscussPost;
import com.wmr.community.entity.Page;
import com.wmr.community.entity.User;
import com.wmr.community.service.CommentService;
import com.wmr.community.service.DiscussPostService;
import com.wmr.community.service.LikeService;
import com.wmr.community.service.UserService;
import com.wmr.community.util.CommunityConstant;
import com.wmr.community.util.CommunityUtil;
import com.wmr.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.*;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant {

    private DiscussPostService discussPostService;

    private UserService userService;

    private CommentService commentService;

    private LikeService likeService;

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
    public void setCommentService(CommentService commentService) {
        this.commentService = commentService;
    }

    @Autowired
    public void setLikeService(LikeService likeService) {
        this.likeService = likeService;
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

    @RequestMapping(path = "/detail/{discussPostId}", method = RequestMethod.GET)
    public ModelAndView showPostDetail(
            @PathVariable(name = "discussPostId") int id,
            @RequestParam(name = "current", required = false, defaultValue = "1") int current,
            Page page
    ) {
        ModelAndView mv = new ModelAndView();
        // 帖子
        DiscussPost discussPost = discussPostService.findDiscussPostById(id);
        // 帖子的点赞
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, id);


        // 判断是否登录
        User loginUser = hostHolder.getUser();
        boolean login = (loginUser != null);
        // 当前登录用户对帖子的点赞状态
        int likeStatus = login ? likeService.findEntityLikeStatus(loginUser.getId(), ENTITY_TYPE_POST, id) : 0;


        // 作者
        User user = null;
        if (discussPost != null) {
            user = userService.findUserById(discussPost.getUserId());
        }
        // 评论的分页
        page.setCurrent(current);
        page.setPath("/discuss/detail/" + id);
        page.setLimit(5);
        page.setRows(commentService.findCommentCount(ENTITY_TYPE_POST, id));
        // 用来将所有的评论及评论中包含的所有回复传给前端
        List<Map<String, Object>> commentPackage = new ArrayList<>();
        // 找到这个帖子的所有评论
        List<Comment> comments = commentService.findComments(ENTITY_TYPE_POST, id, page.getOffset(), page.getLimit());
        // 找到对应评论所有的回复
        for (Comment comment : comments) {
            Map<String, Object> mapComment = new HashMap<>();
            // 评论的信息
            mapComment.put("comment", comment);
            // 评论的点赞数
            mapComment.put("likeCount", likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, comment.getId()));
            // 当前登录用户对评论的点赞状态
            mapComment.put("likeStatus", login ? likeService.findEntityLikeStatus(loginUser.getId(), ENTITY_TYPE_COMMENT, comment.getId()) : 0);

            int count = commentService.findCommentCount(ENTITY_TYPE_COMMENT, comment.getId());
            // 评论的用户信息
            User userComment = userService.findUserById(comment.getUserId());
            mapComment.put("userComment", userComment);
            // 将回复有关的所有信息进行分装
            List<Map<String, Object>> replayPackage = new ArrayList<>();
            // 回复不要分页
            List<Comment> replies = commentService.findComments(ENTITY_TYPE_COMMENT, comment.getId(), 0, 0);
            for (Comment reply : replies) {
                Map<String, Object> mapReply = new HashMap<>();
                // 回复的点赞数
                mapReply.put("likeCount", likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, reply.getId()));
                // 当前登录用户对评论的点赞状态
                mapReply.put("likeStatus", login ? likeService.findEntityLikeStatus(loginUser.getId(), ENTITY_TYPE_COMMENT, reply.getId()) : 0);

                // 回复的用户信息
                User userReply = userService.findUserById(reply.getUserId());
                mapReply.put("userReply", userReply);
                // 回复的对象
                User target = userService.findUserById(reply.getTargetId());
                mapReply.put("target", target);
                // 回复的信息
                mapReply.put("reply", reply);
                replayPackage.add(mapReply);
            }
            // 回复的数量
            mapComment.put("replyCount", count);
            // 回复的信息
            mapComment.put("replyPackage", replayPackage);
            commentPackage.add(mapComment);
        }
        mv.addObject("post", discussPost);
        mv.addObject("likeCount", likeCount);
        mv.addObject("likeStatus", likeStatus);
        mv.addObject("user", user);
        mv.addObject("page", page);
        mv.addObject("commentPackage", commentPackage);
        if (discussPost == null || user == null) {
            mv.setViewName("redirect:/index");
            return mv;
        }
        mv.setViewName("/site/discuss-detail");
        return mv;
    }
}
