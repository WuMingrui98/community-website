package com.wmr.community.config;

import com.wmr.community.quartz.AlphaJob;
import com.wmr.community.quartz.PostScoreScoreRefreshJob;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;


// 配置->数据库->调用
@Configuration
public class QuartzConfig {
    // FactoryBean可简化Bean的实例化过程：
    // 1. 通过FactoryBean封装Bean的实例化过程
    // 2. 将FactoryBean装配到Spring容器中
    // 3. 将FactoryBean注入给其他的Bean
    // 4. 该Bean得到的是FactoryBean所管理的对象实例.

    // 配置JobDetail
//    @Bean
    public JobDetailFactoryBean alphaJobDetail() {
        JobDetailFactoryBean jobDetailFactoryBean = new JobDetailFactoryBean();
        jobDetailFactoryBean.setJobClass(AlphaJob.class);
        jobDetailFactoryBean.setName("alphaJob");
        jobDetailFactoryBean.setGroup("alphaJobGroup");
        // 任务是否持久保存
        jobDetailFactoryBean.setDurability(true);
        // 任务是否可恢复
        jobDetailFactoryBean.setRequestsRecovery(true);
        return jobDetailFactoryBean;
    }

    // 配置Trigger(SimpleTriggerFactoryBean, CronTriggerFactoryBean)
//    @Bean
    public SimpleTriggerFactoryBean alphaTrigger(JobDetail alphaJobDetail) {
        SimpleTriggerFactoryBean simpleTriggerFactoryBean = new SimpleTriggerFactoryBean();
        simpleTriggerFactoryBean.setJobDetail(alphaJobDetail);
        simpleTriggerFactoryBean.setName("alphaTrigger");
        simpleTriggerFactoryBean.setGroup("alphaTriggerGroup");
        simpleTriggerFactoryBean.setRepeatInterval(3000);
        // 底层存储Job对象状态的实例
        simpleTriggerFactoryBean.setJobDataMap(new JobDataMap());
        return simpleTriggerFactoryBean;
    }


    // 配置JobDetail
    @Bean
    public JobDetailFactoryBean postScoreRefreshJobDetail() {
        JobDetailFactoryBean jobDetailFactoryBean = new JobDetailFactoryBean();
        jobDetailFactoryBean.setJobClass(PostScoreScoreRefreshJob.class);
        jobDetailFactoryBean.setName("postScoreScoreRefreshJob");
        jobDetailFactoryBean.setGroup("communityJobGroup");
        // 任务是否持久保存
        jobDetailFactoryBean.setDurability(true);
        // 任务是否可恢复
        jobDetailFactoryBean.setRequestsRecovery(true);
        return jobDetailFactoryBean;
    }

    // 配置Trigger(SimpleTriggerFactoryBean, CronTriggerFactoryBean)
    @Bean
    public SimpleTriggerFactoryBean postScoreRefreshTrigger(JobDetail postScoreRefreshJobDetail) {
        SimpleTriggerFactoryBean simpleTriggerFactoryBean = new SimpleTriggerFactoryBean();
        simpleTriggerFactoryBean.setJobDetail(postScoreRefreshJobDetail);
        simpleTriggerFactoryBean.setName("postScoreRefreshTrigger");
        simpleTriggerFactoryBean.setGroup("communityTriggerGroup");
        // 1分钟执行一次
        simpleTriggerFactoryBean.setRepeatInterval(1000 * 30);
        // 底层存储Job对象状态的实例
        simpleTriggerFactoryBean.setJobDataMap(new JobDataMap());
        return simpleTriggerFactoryBean;
    }

}
