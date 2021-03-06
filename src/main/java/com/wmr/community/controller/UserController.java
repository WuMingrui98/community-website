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
        // ??????????????????
        String filename = CommunityUtil.generateUUID();
        // ??????????????????????????????
        StringMap policy = new StringMap();
        policy.put("returnBody", CommunityUtil.getJSONString(0));
        // ??????????????????
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
        // ????????????????????????
        User user = hostHolder.getUser();
        int userId = user.getId();
        int res = userService.updateHeader(userId, headerUrl);
        if (res != 1) {
            logger.error("??????????????????!");
            return CommunityUtil.getJSONString(1, "??????????????????!");
        }
        else return CommunityUtil.getJSONString(0);
    }


    // ????????????MultipartFile??????????????????
    @Deprecated
    @RequestMapping(path = "/upload", method = RequestMethod.POST)
    public ModelAndView uploadHeadPicture(@RequestParam(name = "head") MultipartFile headPicture) {
        ModelAndView mv = new ModelAndView();
        // ????????????????????????
        if (headPicture == null) {
            mv.addObject("error", "?????????????????????!");
            mv.setViewName("/site/setting");
            return mv;
        }
        // ?????????????????????
        String filename = headPicture.getOriginalFilename();
        String suffix = null;
        if (filename != null) {
            suffix = filename.substring(filename.lastIndexOf("."));
        }
        // ?????????????????????????????????png???jpg???jpeg??????????????????????????????
        if (StringUtils.isBlank(suffix) ||
                !(".png".equals(suffix) || ".jpg".equals(suffix) || ".jpeg".equals(suffix))
        ) {
            mv.addObject("error", "??????????????????!");
            mv.setViewName("/site/setting");
            return mv;
        }

        // ?????????????????????????????????????????????????????????
        filename = CommunityUtil.generateUUID() + suffix;
        // ???????????????????????????
        File dest = new File(picPath, filename);
        try {
            // ??????MultipartFile???api??????????????????
            headPicture.transferTo(dest);
        } catch (IOException e) {
            logger.error("??????????????????: " + e.getMessage());
            throw new RuntimeException("??????????????????????????????????????????!", e);
        }

        // ????????????????????????
        User user = hostHolder.getUser();
        int userId = user.getId();
        String headerUrl = domain + contextPath + "/user/head/" + filename;
        int res = userService.updateHeader(userId, headerUrl);
        if (res != 1) {
            mv.addObject("error", "??????????????????!");
            mv.setViewName("/site/setting");
            return mv;
        }
        mv.setViewName("redirect:/index");
        return mv;
    }

    /**
     * ????????????url????????????????????????????????????????????????????????????
     */
    @RequestMapping(path = "/head/{filename}", method = RequestMethod.GET)
    public void getHeadPicture(@PathVariable(name = "filename") String filename, HttpServletResponse response) {
        // ???????????????????????????
        File dest = new File(picPath, filename);
        // ???????????????
        String suffix = filename.substring(filename.lastIndexOf("."));
        // ????????????
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
            logger.error("??????????????????: " + e.getMessage());
        }
    }

    // ????????????
    @RequestMapping(path = "/password", method = RequestMethod.POST)
    public ModelAndView updatePassword(
            @RequestParam(name = "oldPassword") String oldPassword,
            @RequestParam(name = "newPassword") String newPassword
    ) {
        ModelAndView mv = new ModelAndView();
        // ????????????id
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

    // ????????????
    @RequestMapping(path = "/profile/{userId}", method = RequestMethod.GET)
    public ModelAndView getProfilePage(@PathVariable("userId") int userId) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("????????????????????????!");
        }

        // ??????
        ModelAndView mv = new ModelAndView();
        mv.addObject("user", user);
        // ????????????
        mv.addObject("likeCount", likeService.findUserLikeCount(userId));

        // ????????????
        mv.addObject("followeeCount", followService.findFolloweeCount(userId, ENTITY_TYPE_USER));
        // ????????????
        mv.addObject("followerCount", followService.findFollowerCount(userId, ENTITY_TYPE_USER));
        // ????????????
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

    // ????????????
    @GetMapping(path = "/mypost/{userId}")
    public ModelAndView getMyPost(@PathVariable("userId") int userId, Page page) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("????????????????????????!");
        }

        // ??????page
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

    // ????????????
    @GetMapping(path = "/myreply/{userId}")
    public ModelAndView getMyReply(@PathVariable("userId") int userId, Page page) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("????????????????????????!");
        }

        // ??????page
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
                // ????????????
                if (comment.getEntityType() == 1) {
                    DiscussPost discussPost = discussPostService.findDiscussPostById(comment.getEntityId());
                    map.put("postId", discussPost.getId());
                    map.put("replyContent", discussPost.getContent());
                }
                // ????????????
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
