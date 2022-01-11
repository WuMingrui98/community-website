package com.wmr.community.controller;

import com.wmr.community.entity.Message;
import com.wmr.community.entity.Page;
import com.wmr.community.entity.User;
import com.wmr.community.service.MessageService;
import com.wmr.community.service.UserService;
import com.wmr.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        for (Message letter : letterList) {
            Map<String, Object> map = new HashMap<>();
            map.put("letter", letter);
            map.put("fromUser", userService.findUserById(letter.getFromId()));
            letters.add(map);
        }
        mv.addObject("letters", letters);

        // 设置私信目标
        mv.addObject("target", getLetterTarget(conversationId));

        mv.setViewName("/site/letter-detail");

        return mv;

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
