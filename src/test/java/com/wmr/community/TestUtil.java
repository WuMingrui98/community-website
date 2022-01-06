package com.wmr.community;

import com.wmr.community.util.CommunityUtil;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class TestUtil {
    @Test
    public void testCommunityUtil() {
        Map<String, Object> map = new HashMap<>();
        map.put("nums", new int[] {1, 3, 4});
        String jsonString = CommunityUtil.getJSONString(0, "操作成功", map);
        System.out.println(jsonString);
    }
}
