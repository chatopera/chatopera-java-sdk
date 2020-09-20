package com.chatopera.bot.sdk;

import com.chatopera.bot.utils.EnvUtil;
import org.junit.Assert;
import org.junit.Test;

public class EnvUtilTest {

    @Test
    public void getEnv() {
        int val = EnvUtil.getEnv("UTIL_TEST_INT", 1);
        Assert.assertTrue(val == 1);
    }
}