package com.chatopera.bot.utils;

import org.apache.commons.lang3.StringUtils;

import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

public class Logger {
    final private static String CHATOPERA_SDK_LOG_ENABLED = EnvUtil.getEnv("CHATOPERA_SDK_LOG_TRACE", "off");
    public static boolean enabled = StringUtils.equalsIgnoreCase(CHATOPERA_SDK_LOG_ENABLED, "on");
    final private static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void trace(final String msg) {
        if (enabled) {
            StringBuffer sb = new StringBuffer();
            sb.append(dtf.format(LocalDateTime.now()));
            sb.append(" ");
            sb.append(msg);
            System.out.println(sb.toString());
        }
    }

    public static void warn(final String msg) {
        StringBuffer sb = new StringBuffer();
        sb.append(dtf.format(LocalDateTime.now()));
        sb.append(" WARN ");
        sb.append(msg);
        System.out.println(sb.toString());
    }

    /**
     * Set log trace on or off
     *
     * @param val
     */
    public static void setEnabled(final boolean val) {
        enabled = val;
    }

}
