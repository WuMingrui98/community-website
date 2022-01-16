package com.wmr.community.controller;

import com.wmr.community.annotation.LoginRequired;
import com.wmr.community.entity.User;
import com.wmr.community.service.FollowService;
import com.wmr.community.service.LikeService;
import com.wmr.community.service.UserService;
import com.wmr.community.util.CommunityConstant;
import com.wmr.community.util.CommunityUtil;
import com.wmr.community.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {
    private final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${community.path.head-picture}")
    private String picPath;

    @Value("${community.path.domain}")
    private String domain;

    private UserService userService;

    private LikeService likeService;

    private FollowService followService;

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
    public void setHostHolder(HostHolder hostHolder) {
        this.hostHolder = hostHolder;
    }

    @LoginRequired
    @RequestMapping(path = "/setting", method = RequestMethod.GET)
    public String getSettingPage(){
        return "/site/setting";
    }

    // 需要通过MultipartFile处理上传文件
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
}
