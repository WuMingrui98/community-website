package com.wmr.community.controller;

import com.wmr.community.entity.Message;
import com.wmr.community.entity.Page;
import com.wmr.community.entity.User;
import com.wmr.community.service.MessageService;
import com.wmr.community.service.UserService;
import com.wmr.community.util.CommunityUtil;
import com.wmr.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.*;

@RequestMapping("/letter")
@Controller
public class MessageController {
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

    @RequestMapping(path = "/list", method = RequestMethod.GET)
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
        mv.setViewName("/site/letter");
        return mv;
    }

    @RequestMapping(path = "/detail/{conversationId}")
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

    @RequestMapping(path = "/send", method = RequestMethod.POST)
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

    private String getConversationId(int id1, int id2) {
        return id1 < id2 ? id1 + "_" + id2 : id2 + "_" + id1;
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
}
