package com.wmr.community.service;

import com.wmr.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class DataService {
    private RedisTemplate<String, Object> redisTemplate;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

    @Autowired
    public void setRedisTemplate(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 将指定的ip计入UV
     * @param ip 用户的ip
     */
    public void recordUV(String ip) {
        String uvKey = RedisKeyUtil.getUVKey(dateFormat.format(new Date()));
        redisTemplate.opsForHyperLogLog().add(uvKey, ip);
    }

    /**
     * 统计指定日期范围内的UV
     * @param start 开始日期
     * @param end 结束日期
     * @return 返回统计的UV
     */
    public long calculateUV(Date start, Date end) {
        // 参数判断留在表现层
        // 整理该日期范围内的key
        List<String> keyList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        while (!calendar.getTime().after(end)){
            keyList.add(RedisKeyUtil.getUVKey(dateFormat.format(calendar.getTime())));
            calendar.add(Calendar.DATE, 1);
        }
        String uvKey = RedisKeyUtil.getUVKey(dateFormat.format(start), dateFormat.format(end));
        // 统计该日期范围内的uv
        String[] keys = new String[keyList.size()];
        keyList.toArray(keys);
        redisTemplate.opsForHyperLogLog().union(uvKey, keys);
        return redisTemplate.opsForHyperLogLog().size(uvKey);
    }

    /**
     * 将指定用户计入DAU
     * @param userId 用户id
     */
    public void recordDAU(int userId) {
        String dauKey = RedisKeyUtil.getDAUKey(dateFormat.format(new Date()));
        redisTemplate.opsForValue().setBit(dauKey, userId, true);
    }

    /**
     * 统计指定日期范围内的DAU
     * @param start 开始日期
     * @param end 结束日期
     * @return 返回统计的DAU
     */
    public long calculateDAU(Date start, Date end) {
        // 参数判断留在表现层
        // 整理该日期范围内的key
        List<byte[]> keyList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        while (!calendar.getTime().after(end)){
            keyList.add(RedisKeyUtil.getDAUKey(dateFormat.format(calendar.getTime())).getBytes(StandardCharsets.UTF_8));
            calendar.add(Calendar.DATE, 1);
        }
        byte[] dauKey = RedisKeyUtil.getDAUKey(dateFormat.format(start), dateFormat.format(end)).getBytes(StandardCharsets.UTF_8);
        // 统计该日期范围内的DAU
        byte[][] keys = new byte[keyList.size()][];
        keyList.toArray(keys);
        // 进行or运算
        Object dauNum = redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                connection.bitOp(RedisStringCommands.BitOperation.OR,
                        dauKey, keys);
                return connection.bitCount(dauKey);
            }
        });
        return dauNum == null ? 0 : (Long) dauNum;
    }
}
