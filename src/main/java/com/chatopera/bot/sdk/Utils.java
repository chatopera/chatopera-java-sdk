package com.chatopera.bot.sdk;

import org.apache.commons.lang3.StringUtils;

public class Utils {

    public static <T> T getEnv(final String variable, final T defaultVal) {
        String val = System.getenv(variable);
        if (StringUtils.isBlank(val)) return defaultVal;
        return (T) val;
    }
}
