package com.wmr.community.event;

import com.alibaba.fastjson.JSONObject;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import com.wmr.community.entity.DiscussPost;
import com.wmr.community.entity.Event;
import com.wmr.community.entity.Message;
import com.wmr.community.service.DiscussPostService;
import com.wmr.community.service.ElasticsearchService;
import com.wmr.community.service.MessageService;
import com.wmr.community.util.CommunityConstant;
import com.wmr.community.util.CommunityUtil;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;

@Component
public class EventConsumer implements CommunityConstant {
    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    @Value("${wk.image.command}")
    private String wkImageCommand;

    @Value("${wk.image.storage}")
    private String wkImageStorage;

    @Value("${qiniu.key.access}")
    private String accessKey;

    @Value("${qiniu.key.secret}")
    private String secretKey;

    @Value("${qiniu.bucket.share.name}")
    private String shareBucketName;

    private MessageService messageService;

    private DiscussPostService discussPostService;

    private ElasticsearchService elasticsearchService;

    private ThreadPoolTaskScheduler taskScheduler;

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

    @Autowired
    public void setTaskScheduler(ThreadPoolTaskScheduler taskScheduler) {
        this.taskScheduler = taskScheduler;
    }

    // ???????????????????????????????????????????????????
    @KafkaListener(topics = {TOPIC_COMMENT, TOPIC_LIKE, TOPIC_FOLLOW})
    public void handleMessage(ConsumerRecord<String, String> record) {
        if (record == null || record.value() == null) {
            logger.error("??????????????????!");
            return;
        }
        Event event = JSONObject.parseObject(record.value(), Event.class);
        if (event == null) {
            logger.error("??????????????????!");
            return;
        }

        // ???????????????????????????????????????Message??????
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


    // ???????????????????????????????????????
    @KafkaListener(topics = TOPIC_POST)
    public void handlePostMessage(ConsumerRecord<String, String> record) {
        if (record == null || record.value() == null) {
            logger.error("??????????????????!");
            return;
        }
        Event event = JSONObject.parseObject(record.value(), Event.class);
        if (event == null) {
            logger.error("??????????????????!");
            return;
        }

        DiscussPost discussPost = discussPostService.findDiscussPostById(event.getEntityId());
        elasticsearchService.saveDiscussPost(discussPost);
    }

    // ?????????????????????????????????
    @KafkaListener(topics = TOPIC_DELETE)
    public void handleDeleteMessage(ConsumerRecord<String, String> record) {
        if (record == null || record.value() == null) {
            logger.error("??????????????????!");
            return;
        }
        Event event = JSONObject.parseObject(record.value(), Event.class);
        if (event == null) {
            logger.error("??????????????????!");
            return;
        }

        DiscussPost discussPost = discussPostService.findDiscussPostById(event.getEntityId());
        elasticsearchService.deleteDiscussPost(discussPost);
    }


    // ?????????????????????????????????
    @KafkaListener(topics = TOPIC_SHARE)
    public void handleShareMessage(ConsumerRecord<String, String> record) throws InterruptedException {
        if (record == null || record.value() == null) {
            logger.error("??????????????????!");
            return;
        }
        Event event = JSONObject.parseObject(record.value(), Event.class);
        if (event == null) {
            logger.error("??????????????????!");
            return;
        }

        String htmlUrl = (String) event.getData().get("htmlUrl");
        String filename = (String) event.getData().get("filename");
        String suffix = (String) event.getData().get("suffix");

        String cmd = wkImageCommand + " --quality 75 "
                + htmlUrl + " " + wkImageStorage + "/" + filename + suffix;
        System.out.println(cmd);
        try {
            Runtime.getRuntime().exec(cmd);
            Thread.sleep(1000);
            logger.info("??????????????????!");
        } catch (IOException e) {
            logger.error("??????????????????!" + e.getMessage());
        }

        // ????????????????????????????????????????????????????????????????????????????????????
        UploadTask uploadTask = new UploadTask(filename, suffix);
        ScheduledFuture<?> scheduledFuture = taskScheduler.scheduleAtFixedRate(uploadTask, 500);
        uploadTask.setFuture(scheduledFuture);
    }

    class UploadTask implements Runnable {
        // ????????????
        private String filename;
        // ????????????
        private String suffix;
        // ????????????????????????
        private Future<?> future;
        // ????????????
        private long startTime;
        // ????????????
        private int uploadTimes;

        public UploadTask(String filename, String suffix) {
            this.filename = filename;
            this.suffix = suffix;
            startTime = System.currentTimeMillis();
        }

        public void setFuture(Future<?> future) {
            this.future = future;
        }

        @Override
        public void run() {
            // ????????????
            if (System.currentTimeMillis() - startTime > 30000) {
                logger.error("?????????????????????????????????: " + filename);
                future.cancel(true);
                return;
            }
            // ????????????
            if (uploadTimes >= 3) {
                logger.error("??????????????????,????????????:" + filename);
                future.cancel(true);
                return;
            }
            // ????????????
            String sharePath = wkImageStorage + "/" + filename + suffix;
            File file = new File(sharePath);
            if (file.exists()) {
                logger.info(String.format("?????????%d?????????[%s]", ++uploadTimes, filename));
                // ??????????????????????????????
                StringMap policy = new StringMap();
                policy.put("returnBody", CommunityUtil.getJSONString(0));
                // ??????????????????
                Auth auth = Auth.create(accessKey, secretKey);
                String uploadToken = auth.uploadToken(shareBucketName, filename, 3600, policy);
                // ???????????????
                UploadManager uploadManager = new UploadManager(new Configuration(Zone.zone2()));
                try {
                    // ????????????
                    Response response = uploadManager.put(sharePath, filename, uploadToken, null, "image/png", false);
                    // ???????????????JSON????????????
                    JSONObject jsonObject = JSONObject.parseObject(response.bodyString());
                    System.out.println(response.bodyString());
                    if (jsonObject == null || jsonObject.get("code") == null || !"0".equals(jsonObject.get("code").toString())) {
                        logger.info(String.format("???%d???????????????[%s].", uploadTimes, filename));
                    } else {
                        logger.info(String.format("???%d???????????????[%s].", uploadTimes, filename));
                        // ?????????????????????????????????
                        if (file.delete()) {
                            logger.info("??????: " + filename + "?????????????????????!");
                        } else {
                            logger.error("??????: " + filename + "?????????????????????!");
                        }
                        future.cancel(true);
                    }
                } catch (QiniuException e) {
                    e.printStackTrace();
                }
            } else {
                logger.info("??????????????????[" + filename + "].");
            }
        }
    }
}
