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


import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class ShellUtil {

    public final static String PWD = System.getProperty("user.dir");

    /**
     * 返回当前脚本执行路径
     *
     * @return
     */
    public static String pwd() {
        return PWD;
    }

    /**
     * 运行命令
     *
     * @param cmdline
     * @return
     */
    public static int command(final String cmdline) {
        return command(cmdline, PWD);
    }

    /**
     * 在指定路径运行命令
     * Returns null if it failed for some reason.
     */
    public static int command(final String cmdline,
                              final String directory) {
        ArrayList<String> result = new ArrayList<String>();
        int code = command(cmdline, directory, result);
        StringBuffer sb = new StringBuffer();
        for (final String line : result) {
            sb.append(line);
            sb.append("\n");
        }
        return code;
    }

    /**
     * 在指定路径运行命令并返回标准输出
     * Returns null if it failed for some reason.
     */
    public static int command(final String cmdline,
                              final String directory,
                              final ArrayList<String> result) {
        try {
            Process process =
                    new ProcessBuilder(new String[]{"bash", "-c", cmdline})
                            .redirectErrorStream(true)
                            .directory(new File(directory))
                            .start();

            BufferedReader br = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
            if (result != null) {
                String line = null;
                while ((line = br.readLine()) != null)
                    result.add(line);
            }

            // TODO: There should really be a timeout here.
            int exitCode = process.waitFor();
            return exitCode;
        } catch (Exception e) {
            // Note: doing this is no good in high quality applications.
            // Instead, present appropriate error messages to the user.
            // But it's perfectly fine for prototyping.
            return 10;
        }
    }

}
