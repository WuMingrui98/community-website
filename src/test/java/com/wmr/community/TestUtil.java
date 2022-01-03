package com.wmr.community;

import com.wmr.community.util.CommunityUtil;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

public class TestUtil {
    @Test
    public void testCommunityUtil() {
        System.out.println(CommunityUtil.generateUUID());
        System.out.println(StringUtils.isBlank(" 2  "));
        System.out.println(CommunityUtil.md5("123QQQQQ"));
    }
}
