package com.wmr.community.controller;

import com.wmr.community.entity.*;
import com.wmr.community.event.EventProducer;
import com.wmr.community.service.CommentService;
import com.wmr.community.service.DiscussPostService;
import com.wmr.community.service.LikeService;
import com.wmr.community.service.UserService;
import com.wmr.community.util.CommunityConstant;
import com.wmr.community.util.CommunityUtil;
import com.wmr.community.util.HostHolder;
import com.wmr.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant {

    private DiscussPostService discussPostService;

    private UserService userService;

    private CommentService commentService;

    private LikeService likeService;

    private HostHolder hostHolder;

    private EventProducer eventProducer;

    private RedisTemplate<String, Object> redisTemplate;

    // 牛客纪元
    private static final Date epoch;

    static {
        try {
            epoch = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2014-08-01 00:00:00");
        } catch (ParseException e) {
            throw new RuntimeException("初始化牛客纪元失败!", e);
        }
    }

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

    @Autowired
    public void setEventProducer(EventProducer eventProducer) {
        this.eventProducer = eventProducer;
    }

    @Autowired
    public void setRedisTemplate(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
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
        discussPost.setScore(calScore(discussPost));
        discussPostService.addDiscussPost(discussPost);

        // 触发帖子相关事件
        Event event = new Event()
                .setTopic(TOPIC_POST)
                .setEntityId(discussPost.getId());
        eventProducer.fireEvent(event);


        // 报错的情况,将来统一处理，先假定业务逻辑没有问题
        return CommunityUtil.getJSONString(0, "发布成功");
    }

    /**
     * 计算帖子的分数
     * @param post 帖子
     * @return 返回初始化的帖子的分数
     */
    private double calScore(DiscussPost post) {
        // 分数 = 帖子权重 + 距离天数
        return (post.getCreateTime().getTime() - epoch.getTime()) / (86400000);
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

    // 置顶
    @PostMapping(path = "/top")
    @ResponseBody
    public String top(int id) {
        discussPostService.updateType(id, 1);

        // 触发帖子相关事件
        Event event = new Event()
                .setTopic(TOPIC_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);
        return CommunityUtil.getJSONString(0, "置顶成功!");
    }

    // 取消置顶
    @PostMapping(path = "/notop")
    @ResponseBody
    public String noTop(int id) {
        discussPostService.updateType(id, 0);

        // 触发帖子相关事件
        Event event = new Event()
                .setTopic(TOPIC_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);
        return CommunityUtil.getJSONString(0, "取消置顶成功!");
    }

    // 加精
    @PostMapping(path = "/wonderful")
    @ResponseBody
    public String wonderful(int id) {
        discussPostService.updateStatus(id, 1);

        // 触发帖子相关事件
        Event event = new Event()
                .setTopic(TOPIC_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);

        // 帖子分数需要计算
        String postScoreKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(postScoreKey, id);

        return CommunityUtil.getJSONString(0, "加精成功!");
    }

    // 取消加精
    @PostMapping(path = "/nowonderful")
    @ResponseBody
    public String noWonderful(int id) {
        discussPostService.updateStatus(id, 0);

        // 触发帖子相关事件
        Event event = new Event()
                .setTopic(TOPIC_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);

        // 帖子分数需要计算
        String postScoreKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(postScoreKey, id);

        return CommunityUtil.getJSONString(0, "取消加精成功!");
    }

    // 删除
    @PostMapping(path = "/delete")
    @ResponseBody
    public String delete(int id) {
        discussPostService.updateStatus(id, 2);

        // 触发帖子相关事件
        Event event = new Event()
                .setTopic(TOPIC_DELETE)
                .setEntityId(id);
        eventProducer.fireEvent(event);
        return CommunityUtil.getJSONString(0, "删除成功!");
    }
}
