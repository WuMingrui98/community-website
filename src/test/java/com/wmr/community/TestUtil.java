package com.wmr.community;

import com.wmr.community.util.CommunityUtil;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

public class TestUtil {
    @Test
    public void testCommunityUtil() {
        System.out.println(CommunityUtil.md5("12345678" + "d0ee9"));
    }
}
