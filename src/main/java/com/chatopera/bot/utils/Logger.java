/*
 * Copyright (C) 2018-2023 Chatopera Inc, <https://www.chatopera.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
            if (msg != null) {
                sb.append(msg);
            } else {
                sb.append("null");
            }
            System.out.println(sb.toString());
        }
    }

    public static void warn(final String msg) {
        StringBuffer sb = new StringBuffer();
        sb.append(dtf.format(LocalDateTime.now()));
        sb.append(" WARN ");
        if (msg != null) {
            sb.append(msg);
        } else {
            sb.append("null");
        }
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
