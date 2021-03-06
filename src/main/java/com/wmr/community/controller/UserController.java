package com.wmr.community.controller;

import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import com.wmr.community.annotation.LoginRequired;
import com.wmr.community.entity.Comment;
import com.wmr.community.entity.DiscussPost;
import com.wmr.community.entity.Page;
import com.wmr.community.entity.User;
import com.wmr.community.service.*;
import com.wmr.community.util.CommunityConstant;
import com.wmr.community.util.CommunityUtil;
import com.wmr.community.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {
    private final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${community.path.head-picture}")
    private String picPath;

    @Value("${qiniu.key.access}")
    private String accessKey;

    @Value("${qiniu.key.secret}")
    private String secretKey;

    @Value("${qiniu.bucket.header.name}")
    private String headerBucketName;

    @Value("${qiniu.bucket.header.url}")
    private String headerBucketUrl;

    @Value("${community.path.domain}")
    private String domain;

    private UserService userService;

    private LikeService likeService;

    private FollowService followService;

    private DiscussPostService discussPostService;

    private CommentService commentService;

    private HostHolder hostHolder;


    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    public void setLikeService(LikeService likeService) {
        this.likeService = likeService;
    }

    @Autowired
    public void setFollowService(FollowService followService) {
        this.followService = followService;
    }

    @Autowired
    public void setDiscussPostService(DiscussPostService discussPostService) {
        this.discussPostService = discussPostService;
    }

    @Autowired
    public void setCommentService(CommentService commentService) {
        this.commentService = commentService;
    }

    @Autowired
    public void setHostHolder(HostHolder hostHolder) {
        this.hostHolder = hostHolder;
    }


    @LoginRequired
    @RequestMapping(path = "/setting", method = RequestMethod.GET)
    public String getSettingPage(Model model){
        // 上传文件名称
        String filename = CommunityUtil.generateUUID();
        // 设置犀牛云的响应信息
        StringMap policy = new StringMap();
        policy.put("returnBody", CommunityUtil.getJSONString(0));
        // 生成上传凭证
        Auth auth = Auth.create(accessKey, secretKey);
        String uploadToken = auth.uploadToken(headerBucketName, filename, 3600, policy);
        model.addAttribute("uploadToken", uploadToken);
        model.addAttribute("filename", filename);
        return "/site/setting";
    }

    @PostMapping(path = "/header/url")
    @ResponseBody
    public String updateHeaderUrl(@RequestParam(value = "filename") String filename) {
        String headerUrl = headerBucketUrl + "/" + filename;
        // 更新一下数据库中
        User user = hostHolder.getUser();
        int userId = user.getId();
        int res = userService.updateHeader(userId, headerUrl);
        if (res != 1) {
            logger.error("更新头像失败!");
            return CommunityUtil.getJSONString(1, "更新头像失败!");
        }
        else return CommunityUtil.getJSONString(0);
    }


    // 需要通过MultipartFile处理上传文件
    @Deprecated
    @RequestMapping(path = "/upload", method = RequestMethod.POST)
    public ModelAndView uploadHeadPicture(@RequestParam(name = "head") MultipartFile headPicture) {
        ModelAndView mv = new ModelAndView();
        // 未上传头像的情况
        if (headPicture == null) {
            mv.addObject("error", "您还未选择图片!");
            mv.setViewName("/site/setting");
            return mv;
        }
        // 头像格式不正确
        String filename = headPicture.getOriginalFilename();
        String suffix = null;
        if (filename != null) {
            suffix = filename.substring(filename.lastIndexOf("."));
        }
        // 后缀为空，或者后缀不为png、jpg、jpeg就表示文件格式不正确
        if (StringUtils.isBlank(suffix) ||
                !(".png".equals(suffix) || ".jpg".equals(suffix) || ".jpeg".equals(suffix))
        ) {
            mv.addObject("error", "文件格式错误!");
            mv.setViewName("/site/setting");
            return mv;
        }

        // 将头像文件保存一下，头像名需要是随机的
        filename = CommunityUtil.generateUUID() + suffix;
        // 确定头像的存放路径
        File dest = new File(picPath, filename);
        try {
            // 使用MultipartFile的api完成文件存储
            headPicture.transferTo(dest);
        } catch (IOException e) {
            logger.error("上传头像失败: " + e.getMessage());
            throw new RuntimeException("上传文件失败，服务器出现异常!", e);
        }

        // 更新一下数据库中
        User user = hostHolder.getUser();
        int userId = user.getId();
        String headerUrl = domain + contextPath + "/user/head/" + filename;
        int res = userService.updateHeader(userId, headerUrl);
        if (res != 1) {
            mv.addObject("error", "文件上传失败!");
            mv.setViewName("/site/setting");
            return mv;
        }
        mv.setViewName("redirect:/index");
        return mv;
    }

    /**
     * 将头像的url和服务器存放位置进行对应，并传输给浏览器
     */
    @RequestMapping(path = "/head/{filename}", method = RequestMethod.GET)
    public void getHeadPicture(@PathVariable(name = "filename") String filename, HttpServletResponse response) {
        // 确定头像的存放路径
        File dest = new File(picPath, filename);
        // 文件的后缀
        String suffix = filename.substring(filename.lastIndexOf("."));
        // 响应类型
        response.setContentType("image/" + suffix);
        try (
                FileInputStream fileInputStream = new FileInputStream(dest);
                ServletOutputStream outputStream = response.getOutputStream()
        ) {
            byte[] buff = new byte[1024];
            int len;
            while ((len = fileInputStream.read(buff)) != -1) {
                outputStream.write(buff, 0, len);
            }
        } catch (IOException e) {
            logger.error("读取头像失败: " + e.getMessage());
        }
    }

    // 更新密码
    @RequestMapping(path = "/password", method = RequestMethod.POST)
    public ModelAndView updatePassword(
            @RequestParam(name = "oldPassword") String oldPassword,
            @RequestParam(name = "newPassword") String newPassword
    ) {
        ModelAndView mv = new ModelAndView();
        // 获取用户id
        User user = hostHolder.getUser();
        int id = user.getId();
        String error = userService.updatePassword(id, oldPassword, newPassword);
        if (error != null) {
            mv.addObject("pwdError", error);
            mv.setViewName("/site/setting");
            return mv;
        }
        mv.setViewName("redirect:/index");
        return mv;
    }

    // 用户主页
    @RequestMapping(path = "/profile/{userId}", method = RequestMethod.GET)
    public ModelAndView getProfilePage(@PathVariable("userId") int userId) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("用户的主页不存在!");
        }

        // 用户
        ModelAndView mv = new ModelAndView();
        mv.addObject("user", user);
        // 点赞数量
        mv.addObject("likeCount", likeService.findUserLikeCount(userId));

        // 关注数量
        mv.addObject("followeeCount", followService.findFolloweeCount(userId, ENTITY_TYPE_USER));
        // 粉丝数量
        mv.addObject("followerCount", followService.findFollowerCount(userId, ENTITY_TYPE_USER));
        // 关注状态
        User loginUser = hostHolder.getUser();
        boolean hasFollowed = true;
        if (loginUser == null) {
            hasFollowed = false;
        } else {
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
        }
        mv.addObject("hasFollowed", hasFollowed);
        mv.setViewName("/site/profile");
        return mv;
    }

    // 我的帖子
    @GetMapping(path = "/mypost/{userId}")
    public ModelAndView getMyPost(@PathVariable("userId") int userId, Page page) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("用户的帖子不存在!");
        }

        // 设置page
        page.setPath("/user/mypost/" + userId);
        int postRows = discussPostService.findDiscussPostRows(userId);
        page.setRows(postRows);

        ModelAndView mv = new ModelAndView();
        mv.addObject("user", user);
        mv.addObject("postRows", postRows);
        List<DiscussPost> list = discussPostService.findDiscussPosts(userId, page.getOffset(), page.getLimit(), 0);
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if (list != null) {
            for (DiscussPost discussPost : list) {
                Map<String, Object> map = new HashMap<>();
                long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, discussPost.getId());
                map.put("post", discussPost);
                map.put("likeCount", likeCount);
                discussPosts.add(map);
            }
        }
        mv.addObject("page", page);
        mv.addObject("discussPosts", discussPosts);
        mv.setViewName("/site/my-post");
        return mv;
    }

    // 我的回复
    @GetMapping(path = "/myreply/{userId}")
    public ModelAndView getMyReply(@PathVariable("userId") int userId, Page page) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("用户的回复不存在!");
        }

        // 设置page
        page.setPath("/user/myreply/" + userId);
        int commentCount = commentService.findCommentCountByUserId(userId);
        page.setRows(commentCount);
        ModelAndView mv = new ModelAndView();
        mv.addObject("commentCount", commentCount);
        mv.addObject("user", user);

        List<Comment> list = commentService.findCommentByUserId(userId, page.getOffset(), page.getLimit());
        List<Map<String, Object>> comments = new ArrayList<>();
        if (list != null) {
            for (Comment comment : list) {
                Map<String, Object> map = new HashMap<>();
                // 表示评论
                if (comment.getEntityType() == 1) {
                    DiscussPost discussPost = discussPostService.findDiscussPostById(comment.getEntityId());
                    map.put("postId", discussPost.getId());
                    map.put("replyContent", discussPost.getContent());
                }
                // 表示回复
                if (comment.getEntityType() == 2) {
                    Comment commentReply = commentService.findCommentById(comment.getEntityId());
                    map.put("replyContent", commentReply.getContent());
                    DiscussPost discussPost = discussPostService.findDiscussPostById(commentReply.getEntityId());
                    map.put("postId", discussPost.getId());
                }
                map.put("comment", comment);
                comments.add(map);
            }
        }
        mv.addObject("page", page);
        mv.addObject("comments", comments);
        mv.setViewName("/site/my-reply");
        return mv;
    }
}
