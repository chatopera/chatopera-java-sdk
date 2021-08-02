/*
 * Copyright (C) 2018-2021 Chatopera Inc, <https://www.chatopera.com>
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


import com.chatopera.bot.exception.FileNotExistException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileUtil {

    /**
     * 指定路径是否存在
     *
     * @param path
     * @return
     */
    public static boolean exists(final String path) {
        File f = new File(path);
        return f.exists();
    }

    /**
     * 指定路径是否是文件夹
     *
     * @param path
     * @return
     * @throws FileNotExistException
     */
    public static boolean isDirectory(final String path) throws FileNotExistException {
        File f = new File(path);
        if (!f.exists()) {
            throw new FileNotExistException("File not exist.");
        }
        return f.isDirectory();
    }

    /**
     * 指定路径是否是文件
     *
     * @param path
     * @return
     * @throws FileNotExistException
     */
    public static boolean isFile(final String path) throws FileNotExistException {
        File f = new File(path);
        if (!f.exists()) {
            throw new FileNotExistException("File not exist.");
        }
        return f.isFile();
    }

    /**
     * https://www.journaldev.com/709/java-read-file-line-by-line
     *
     * @param filePath
     * @param istrim
     * @return
     * @throws FileNotExistException
     */
    public static List<String> readlines(final String filePath, final boolean istrim) throws FileNotExistException {
        if (StringUtils.isBlank(filePath))
            throw new FileNotExistException("Blank file path param is invalid.");

        if (isFile(filePath)) {
            List<String> ret = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new FileReader(
                    filePath))) {
                String line = reader.readLine();
                while (line != null) {
                    ret.add(istrim ? line.trim() : line);
                    // read next line
                    line = reader.readLine();
                }
                reader.close();
                return ret;
            } catch (IOException e) {
                throw new FileNotExistException("IOException", e);
            }
        } else {
            throw new FileNotExistException("Path is not file but directory.");
        }
    }

    /**
     * @param filePath
     * @return
     * @throws FileNotExistException
     */
    public static List<String> readlines(final String filePath) throws FileNotExistException {
        return readlines(filePath, false);
    }

    /**
     * 指定路径文件写入内容，覆盖以前内容
     *
     * @param filePath
     * @param lines
     * @throws IOException
     */
    public static void writelines(final String filePath, final List<String> lines) throws IOException {
        File fout = new File(filePath);
        FileOutputStream fos = new FileOutputStream(fout);

        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

        for (int i = 0; i < lines.size(); i++) {
            bw.write(lines.get(i));
            bw.newLine();
        }

        bw.close();
    }

    /**
     * 删除指定路径
     *
     * @param path
     */
    public static void remove(final String path) throws IOException {
        if (exists(path))
            ShellUtil.command("rm -rf " + path);
    }

    /**
     * 移动文件
     *
     * @param pre
     * @param post
     */
    public static int move(final String pre, final String post) throws FileNotExistException {
        if (exists(pre) && StringUtils.isNotBlank(post)) {
            return ShellUtil.command("mv " + pre + " " + post);
        } else {
            throw new FileNotExistException("File or directory not exist OR target path is empty string.");
        }
    }

    /**
     * 复制文件
     *
     * @param source
     * @param copied
     */
    public static int copy(final String source, final String copied) throws FileNotExistException {
        if (exists(source) && StringUtils.isNotBlank(copied)) {
            return ShellUtil.command("cp -rf " + source + " " + copied);
        } else {
            throw new FileNotExistException("File or directory not exist OR copied path is empty string.");
        }
    }

    /**
     * 创建路径软链接
     * https://javarevisited.blogspot.com/2011/04/symbolic-link-or-symlink-in-unix-linux.html
     *
     * @param source
     * @param link
     * @return
     * @throws FileNotExistException
     * @throws IOException
     */
    public static int ln(final String source, final String link) throws FileNotExistException, IOException {
        if (exists(source) && StringUtils.isNotBlank(link)) {
            if (FileUtils.isSymlink(new File(link))) {
                // 更新
                return ShellUtil.command("ln -nsf " + source + " " + link);
            } else if (FileUtil.exists(link)) {
                FileUtil.remove(link);
            }
            // 新建
            return ShellUtil.command("ln -s " + source + " " + link);
        } else {
            throw new FileNotExistException("File or directory not exist OR copied path is empty string." + " Source " + source + ", target " + link);
        }
    }

}