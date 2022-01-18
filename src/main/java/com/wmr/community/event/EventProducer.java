package com.wmr.community.event;

import com.alibaba.fastjson.JSONObject;
import com.wmr.community.entity.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class EventProducer {
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    public void setKafkaTemplate(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    // 激活事件
    public void fireEvent(Event event) {
        // 将事件发布到指定topic的队列
        kafkaTemplate.send(event.getTopic(), JSONObject.toJSONString(event));
    }
}
