package com.wmr.community.util;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.security.MD5Encoder;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

public class CommunityUtil {
    /**
     * 获得唯一标识的UUID
     * @return 返回唯一标识的UUID
     */
    public static String generateUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * MD5加密
     * @param password 密码
     * @return 返回经过MD5机密后的密码
     */
    public static String md5(String password) {
        if (StringUtils.isBlank(password)) {
            return null;
        }
        return DigestUtils.md5DigestAsHex(password.getBytes(StandardCharsets.UTF_8));
    }


    /**
     * 获得传给客户端的json字符串
     *
     * @param code 状态码
     * @param msg 信息
     * @param map 其他需要传回客户端的信息
     * @return 返回传给客户端的json字符串
     */
    public static String getJSONString(int code, String msg, Map<String, Object> map) {
        JSONObject json = new JSONObject();
        json.put("code", code);
        json.put("msg", msg);
        if (map != null) {
            for (String key: map.keySet()) {
                json.put(key, map.get(key));
            }
        }
        return json.toJSONString();
    }

    /**
     * 获得传给客户端的json字符串（方法重载）
     * @param code 状态码
     * @param msg 信息
     * @return 返回传给客户端的json字符串
     */
    public static String getJSONString(int code, String msg) {
        return getJSONString(code, msg, null);
    }

    /**
     * 获得传给客户端的json字符串（方法重载）
     * @param code 状态码
     * @return 返回传给客户端的json字符串
     */
    public static String getJSONString(int code) {
        return getJSONString(code, null, null);
    }
}
