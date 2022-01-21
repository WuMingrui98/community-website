package com.wmr.community.controller;

import com.wmr.community.service.DataService;
import com.wmr.community.util.CommunityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;

@Controller
public class DataController {

    private DataService dataService;

    @Autowired
    public void setDataService(DataService dataService) {
        this.dataService = dataService;
    }

    // 统计页面
    @GetMapping(path = "/data")
    public String getDataPage() {
        return "/site/admin/data";
    }

    // 统计网站的UV
    @PostMapping(path = "/data/uv")
    @ResponseBody
    public String getUV(@DateTimeFormat(pattern = "yyyy-MM-dd") Date start,
                        @DateTimeFormat(pattern = "yyyy-MM-dd") Date end) {
        if (start == null || end == null) {
            return CommunityUtil.getJSONString(1, "参数不能为空!");
        }
        if (start.after(end)) {
            return CommunityUtil.getJSONString(1, "开始时间要在结束时间之前!");
        }
        long uv = dataService.calculateUV(start, end);
        return CommunityUtil.getJSONString(0, uv + "");
    }

    // 统计活跃用户
    @PostMapping(path = "/data/dau")
    @ResponseBody
    public String getDAU(@DateTimeFormat(pattern = "yyyy-MM-dd") Date start,
                        @DateTimeFormat(pattern = "yyyy-MM-dd") Date end) {
        if (start == null || end == null) {
            return CommunityUtil.getJSONString(1, "参数不能为空!");
        }
        if (start.after(end)) {
            return CommunityUtil.getJSONString(1, "开始时间要在结束时间之前!");
        }
        long dau = dataService.calculateDAU(start, end);
        return CommunityUtil.getJSONString(0, dau + "");
    }
}
