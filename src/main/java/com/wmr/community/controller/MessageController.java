package com.wmr.community.controller;

import com.alibaba.fastjson.JSONObject;
import com.wmr.community.entity.Message;
import com.wmr.community.entity.Page;
import com.wmr.community.entity.User;
import com.wmr.community.service.MessageService;
import com.wmr.community.service.UserService;
import com.wmr.community.util.CommunityConstant;
import com.wmr.community.util.CommunityUtil;
import com.wmr.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.HtmlUtils;

import java.util.*;

@Controller
public class MessageController implements CommunityConstant {
    private HostHolder hostHolder;

    private MessageService messageService;

    private UserService userService;

    @Autowired
    public void setHostHolder(HostHolder hostHolder) {
        this.hostHolder = hostHolder;
    }

    @Autowired
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @RequestMapping(path = "/letter/list", method = RequestMethod.GET)
    public ModelAndView getConversationList(Page page) {
        ModelAndView mv = new ModelAndView();
        User user = hostHolder.getUser();
        // 设置分页
        page.setLimit(5);
        page.setRows(messageService.findConversationCount(user.getId()));
        page.setPath("/letter/list");
        // 用一个list来封装返回的信息
        List<Map<String, Object>> conversations = new ArrayList<>();
        // 查询会话列表
        List<Message> conversationList = messageService.findConversations(user.getId(), page.getOffset(), page.getLimit());
        for (Message conversation: conversationList) {
            Map<String, Object> map = new HashMap<>();
            map.put("conversation", conversation);
            map.put("lettetCount", messageService.findLetterCount(conversation.getConversationId()));
            map.put("unreadCount", messageService.findLetterUnreadCount(user.getId(), conversation.getConversationId()));
            int targetId = user.getId() != conversation.getFromId() ? conversation.getFromId() : conversation.getToId();
            map.put("target", userService.findUserById(targetId));
            conversations.add(map);
        }
        mv.addObject("conversations", conversations);
        // 查询未读消息
        int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
        mv.addObject("letterUnreadCount", letterUnreadCount);
        // 总的未读系统通知数量
        mv.addObject("noticeUnreadCount", messageService.findNoticeUnreadCount(user.getId(), null));
        mv.setViewName("/site/letter");
        return mv;
    }

    @RequestMapping(path = "/letter/detail/{conversationId}")
    public ModelAndView getLetterDetail(@PathVariable(name = "conversationId") String conversationId, Page page) {
        ModelAndView mv = new ModelAndView();
        User user = hostHolder.getUser();
        // 设置分页
        page.setLimit(5);
        page.setRows(messageService.findLetterCount(conversationId));
        page.setPath("/letter/detail/" + conversationId);

        // 用一个list来封装返回的信息
        List<Map<String, Object>> letters = new ArrayList<>();
        // 查询私信列表
        List<Message> letterList = messageService.findLetters(conversationId, page.getOffset(), page.getLimit());
        // 未读私信id列表
        List<Integer> unreadIds = new ArrayList<>();

        for (Message letter : letterList) {
            // 如果私信的状态为0则加入到未读私信id列表，还要判断私信的toId是否为当前的user
            if (letter.getStatus() == 0 && letter.getToId() == user.getId()) unreadIds.add(letter.getId());

            Map<String, Object> map = new HashMap<>();
            map.put("letter", letter);
            map.put("fromUser", userService.findUserById(letter.getFromId()));
            letters.add(map);
        }
        if (!unreadIds.isEmpty()) {
            // 将查询的id设置为已读
            messageService.readMessage(unreadIds);
        }


        mv.addObject("letters", letters);
        // 设置私信目标
        mv.addObject("target", getLetterTarget(conversationId));

        mv.setViewName("/site/letter-detail");

        return mv;

    }

    @RequestMapping(path = "/letter/send", method = RequestMethod.POST)
    @ResponseBody
    public String sendLetter(@RequestParam(name = "toName") String toName,
                             @RequestParam(name = "content") String content) {
        User toUser = userService.findUserByName(toName);
        if (toUser == null) {
            return CommunityUtil.getJSONString(1, "用户不存在!");
        }
        User fromUser = hostHolder.getUser();
        Message message = new Message();
        message.setContent(content);
        message.setCreateTime(new Date());
        message.setStatus(0);
        message.setFromId(fromUser.getId());
        message.setToId(toUser.getId());
        // 设置会话id
        message.setConversationId();
        int res = messageService.addMessage(message);
        if (res > 0) {
            return CommunityUtil.getJSONString(0);
        } else {
            return CommunityUtil.getJSONString(1, "发送失败!");
        }
    }

    private User getLetterTarget(String conversationId) {
        String[] ids = conversationId.split("_");
        int id0 = Integer.parseInt(ids[0]);
        int id1 = Integer.parseInt(ids[1]);

        if (hostHolder.getUser().getId() == id0) {
            return userService.findUserById(id1);
        } else {
            return userService.findUserById(id0);
        }
    }

    @GetMapping(path = "/notice/list")
    public ModelAndView getNoticeList() {
        ModelAndView mv = new ModelAndView();
        User user = hostHolder.getUser();
        // 评论通知
        Map<String, Object> noticeMap = new HashMap<>();
        // 最新评论
        Message latestNotice = messageService.findLatestNotice(user.getId(), TOPIC_COMMENT);
        if (latestNotice != null) {
            // 将最新通知放入map
            noticeMap.put("latestNotice", latestNotice);
            // 解析latestNotice的内容
            String content = HtmlUtils.htmlUnescape(latestNotice.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
            noticeMap.put("user", userService.findUserById((Integer) data.get("userId")));
            noticeMap.put("entityType", data.get("entityType"));
            noticeMap.put("entityId", data.get("entityId"));
            noticeMap.put("postId", data.get("postId"));
            // 通知数量
            int count = messageService.findNoticeCount(user.getId(), TOPIC_COMMENT);
            noticeMap.put("count", count);
            // 未读通知数量
            int unreadCount = messageService.findNoticeUnreadCount(user.getId(), TOPIC_COMMENT);
            noticeMap.put("unreadCount", unreadCount);
        }
        mv.addObject("commentNotice", noticeMap);

        // 赞通知
        noticeMap = new HashMap<>();
        // 最新评论
        latestNotice = messageService.findLatestNotice(user.getId(), TOPIC_LIKE);
        if (latestNotice != null) {
            // 将最新通知放入map
            noticeMap.put("latestNotice", latestNotice);
            // 解析latestNotice的内容
            String content = HtmlUtils.htmlUnescape(latestNotice.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
            noticeMap.put("user", userService.findUserById((Integer) data.get("userId")));
            noticeMap.put("entityType", data.get("entityType"));
            noticeMap.put("entityId", data.get("entityId"));
            noticeMap.put("postId", data.get("postId"));
            // 通知数量
            int count = messageService.findNoticeCount(user.getId(), TOPIC_LIKE);
            noticeMap.put("count", count);
            // 未读通知数量
            int unreadCount = messageService.findNoticeUnreadCount(user.getId(), TOPIC_LIKE);
            noticeMap.put("unreadCount", unreadCount);
        }
        mv.addObject("likeNotice", noticeMap);

        // 关注通知
        noticeMap = new HashMap<>();
        // 最新评论
        latestNotice = messageService.findLatestNotice(user.getId(), TOPIC_FOLLOW);
        if (latestNotice != null) {
            // 将最新通知放入map
            noticeMap.put("latestNotice", latestNotice);
            // 解析latestNotice的内容
            String content = HtmlUtils.htmlUnescape(latestNotice.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
            noticeMap.put("user", userService.findUserById((Integer) data.get("userId")));
            noticeMap.put("entityType", data.get("entityType"));
            noticeMap.put("entityId", data.get("entityId"));
            // 通知数量
            int count = messageService.findNoticeCount(user.getId(), TOPIC_FOLLOW);
            noticeMap.put("count", count);
            // 未读通知数量
            int unreadCount = messageService.findNoticeUnreadCount(user.getId(), TOPIC_FOLLOW);
            noticeMap.put("unreadCount", unreadCount);
        }
        mv.addObject("followNotice", noticeMap);

        // 总的未读系统通知数量
        mv.addObject("noticeUnreadCount", messageService.findNoticeUnreadCount(user.getId(), null));
        // 总的未读私信数量
        mv.addObject("letterUnreadCount", messageService.findLetterUnreadCount(user.getId(), null));

        mv.setViewName("/site/notice");
        return mv;
    }

    @GetMapping(path = "/notice/detail/{topic}")
    public ModelAndView getNoticeDetail(@PathVariable("topic") String topic, Page page) {
        ModelAndView mv = new ModelAndView();
        User user = hostHolder.getUser();
        // 设置分页
        page.setLimit(5);
        page.setRows(messageService.findNoticeCount(user.getId(), topic));
        page.setPath("/notice/detail/" + topic);

        // 用一个list来封装返回的信息
        List<Map<String, Object>> notices = new ArrayList<>();
        // 查询通知列表
        List<Message> noticeList = messageService.findNotices(user.getId(), topic, page.getOffset(), page.getLimit());
        // 未读通知id列表
        List<Integer> unreadIds = new ArrayList<>();

        for (Message notice : noticeList) {
            // 如果私信的状态为0则加入到未读通知id列表
            if (notice.getStatus() == 0) unreadIds.add(notice.getId());
            Map<String, Object> map = new HashMap<>();
            map.put("notice", notice);
            // 解析通知内容
            String content = HtmlUtils.htmlUnescape(notice.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
            map.put("user", userService.findUserById((Integer) data.get("userId")));
            map.put("entityType", data.get("entityType"));
            map.put("entityId", data.get("entityId"));
            map.put("postId", data.get("postId"));
            // 系统信息
            map.put("fromUser", userService.findUserById(SYSTEM_USER_ID));
            notices.add(map);
        }
        if (!unreadIds.isEmpty()) {
            // 将查询的id设置为已读
            messageService.readMessage(unreadIds);
        }

        mv.addObject("notices", notices);

        mv.setViewName("/site/notice-detail");

        return mv;
    }
}
