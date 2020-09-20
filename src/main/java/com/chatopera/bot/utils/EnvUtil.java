package com.chatopera.bot.utils;

import org.apache.commons.lang3.StringUtils;

public class EnvUtil {

    public static <T> T getEnv(final String variable, final T defaultVal) {
        String val = System.getenv(variable);
        if (StringUtils.isBlank(val)) return defaultVal;
        return (T) val;
    }
}
