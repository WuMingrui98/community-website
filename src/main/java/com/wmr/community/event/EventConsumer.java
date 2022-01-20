package com.wmr.community.event;

import com.alibaba.fastjson.JSONObject;
import com.wmr.community.entity.DiscussPost;
import com.wmr.community.entity.Event;
import com.wmr.community.entity.Message;
import com.wmr.community.service.DiscussPostService;
import com.wmr.community.service.ElasticsearchService;
import com.wmr.community.service.MessageService;
import com.wmr.community.util.CommunityConstant;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class EventConsumer implements CommunityConstant {
    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    private MessageService messageService;

    private DiscussPostService discussPostService;

    private ElasticsearchService elasticsearchService;

    @Autowired
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    @Autowired
    public void setDiscussPostService(DiscussPostService discussPostService) {
        this.discussPostService = discussPostService;
    }

    @Autowired
    public void setElasticsearchService(ElasticsearchService elasticsearchService) {
        this.elasticsearchService = elasticsearchService;
    }

    // 处理评论、点赞和关注事件的回调函数
    @KafkaListener(topics = {TOPIC_COMMENT, TOPIC_LIKE, TOPIC_FOLLOW})
    public void handleMessage(ConsumerRecord<String, String> record) {
        if (record == null || record.value() == null) {
            logger.error("消息内容为空!");
            return;
        }
        Event event = JSONObject.parseObject(record.value(), Event.class);
        if (event == null) {
            logger.error("消息格式错误!");
            return;
        }

        // 将事件中封装的信息再封装为Message对象
        Message message = new Message();
        message.setFromId(SYSTEM_USER_ID);
        message.setToId(event.getEntityUserId());
        message.setConversationId(event.getTopic());
        message.setCreateTime(new Date());

        Map<String, Object> content = new HashMap<>();
        content.put("userId", event.getUserId());
        content.put("entityType", event.getEntityType());
        content.put("entityId", event.getEntityId());

        if (!event.getData().isEmpty()) {
            content.putAll(event.getData());
        }

        message.setContent(JSONObject.toJSONString(content));
        messageService.addMessage(message);
    }


    // 处理发帖事件的回调函数
    @KafkaListener(topics = TOPIC_POST)
    public void handlePostMessage(ConsumerRecord<String, String> record) {
        if (record == null || record.value() == null) {
            logger.error("消息内容为空!");
            return;
        }
        Event event = JSONObject.parseObject(record.value(), Event.class);
        if (event == null) {
            logger.error("消息格式错误!");
            return;
        }

        DiscussPost discussPost = discussPostService.findDiscussPostById(event.getEntityId());
        elasticsearchService.saveDiscussPost(discussPost);
    }
}
