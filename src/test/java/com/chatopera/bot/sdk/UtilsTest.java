package com.chatopera.bot.sdk;

import org.junit.Assert;
import org.junit.Test;

public class UtilsTest {

    @Test
    public void getEnv() {
        int val = Utils.getEnv("UTIL_TEST_INT", 1);
        Assert.assertTrue(val == 1);
    }
}