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
package com.chatopera.bot.sdk;

import com.chatopera.bot.sdk.basics.Constants;
import com.chatopera.bot.sdk.basics.Response;
import com.chatopera.bot.exception.ChatbotException;
import com.chatopera.bot.sdk.basics.RestAPI;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

/**
 * Chatopera 高级管理
 * 使用 Access Token 管理 Chatopera 机器人平台的对应账户下的资源，比如创建机器人、获得机器人列表
 * 因为此接口可以删除不可逆的资源，需要谨慎使用。
 */
public class Chatopera {
    private final String BASE_PATH = "/api/v1";
    private String schema;
    private String hostname;
    private int port;
    private String baseUrl;
    private String accessToken;

    // 不支持定义空实例
    private Chatopera() {
    }


    /**
     * @param accessToken
     * @param baseUrl
     * @throws ChatbotException
     * @throws MalformedURLException
     */
    public Chatopera(final String accessToken, final String baseUrl) throws ChatbotException, MalformedURLException {
        if (StringUtils.isBlank(baseUrl)) {
            throw new ChatbotException("智能问答引擎URL不能为空。");
        }

        if (StringUtils.isNotBlank(accessToken)) {
            this.accessToken = StringUtils.trim(accessToken);
        } else {
            throw new ChatbotException("Invalid access token.");
        }

        parseEndpoint(baseUrl);
    }

    /**
     * @param accessToken
     * @throws MalformedURLException
     * @throws ChatbotException
     */
    public Chatopera(final String accessToken) throws ChatbotException, MalformedURLException {
        this(accessToken, Constants.defaultBaseUrl);
    }

    /**
     * 分析URL信息
     *
     * @param url
     * @throws MalformedURLException
     */
    private void parseEndpoint(final String url) throws MalformedURLException {
        URL uri = new URL(url);
        this.schema = uri.getProtocol();
        this.hostname = uri.getHost();
        this.port = uri.getPort();

        if (port == -1) {
            this.baseUrl = this.schema + "://" + this.hostname + BASE_PATH;
        } else {
            this.baseUrl = this.schema + "://" + this.hostname + ":" + this.port + BASE_PATH;
        }
    }


    public String getSchema() {
        return schema;
    }

    public String getHostname() {
        return hostname;
    }

    public int getPort() {
        return port;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * 生成认证信息
     *
     * @param accessToken
     * @return
     * @throws Exception
     */
    private HashMap<String, String> auth(final String accessToken) {
        System.out.println("[auth] accessToken " + accessToken);
        HashMap<String, String> x = new HashMap<>();
        StringBuffer sb = new StringBuffer();
        sb.append("Bearer ");
        sb.append(accessToken);
        x.put("Authorization", sb.toString());
        return x;
    }

    /**
     * 核心访问接口
     *
     * @param method  PUT, POST, GET, DELETE, etc.
     * @param path    /faq/xxx
     * @param payload JSONObject body
     * @return
     * @throws ChatbotException
     */
    public Response command(final String method, String path, final JSONObject payload) throws ChatbotException {
        /**
         * 准备参数
         */
        StringBuffer url = getUrlPrefix();

        // 自动添加 sdklang 参数
        if (StringUtils.isNotBlank(path)) {
            String[] pairs = path.split("&");
            if (pairs.length > 1 && path.contains("?")) {
                path += "&sdklang=java";
            } else {
                path += "?sdklang=java";
            }
        } else {
            path = "/?sdklang=java";
        }

        if (StringUtils.isNotBlank(path)) {
            url.append(path);
        }

        /**
         * 发送请求
         */
        JSONObject result;
        try {
            switch (method) {
                case "GET":
                    result = RestAPI.get(url.toString(), null, auth(this.accessToken));
                    break;
                case "POST":
                    result = RestAPI.post(url.toString(), payload, null, auth(this.accessToken));
                    break;
                case "DELETE":
                    result = RestAPI.delete(url.toString(), auth(this.accessToken));
                    break;
                case "PUT":
                    result = RestAPI.put(url.toString(), payload, null, auth(this.accessToken));
                    break;
                default:
                    throw new ChatbotException("Invalid requested method, only GET, POST, DELETE, PUT are supported.");
            }
        } catch (Exception e) {
            throw new ChatbotException(e.toString());
        }

        /**
         * 处理返回值
         */
        purge(result);
        Response resp = new Response();
        resp.setRc(result.getInt("rc"));

        if (result.has("error")) {
            try {
                resp.setError(result.getString("error"));
            } catch (Exception e) {
                resp.setError(result.get("error").toString());
            }
        }

        if (result.has("msg"))
            resp.setMsg(result.getString("msg"));

        if (result.has("data"))
            resp.setData(result.get("data"));

        if (result.has("total"))
            resp.setTotal(result.getInt("total"));

        if (result.has("current_page"))
            resp.setTotal(result.getInt("current_page"));

        if (result.has("total_page"))
            resp.setTotal(result.getInt("total_page"));

        return resp;
    }

    /**
     * 核心访问接口
     *
     * @param method PUT, POST, etc.
     * @param path   /chatbot
     * @return
     * @throws ChatbotException
     */
    public Response command(final String method, final String path) throws ChatbotException {
        return command(method, path, null);
    }


    /**
     * remove data
     *
     * @param j
     */
    private void purge(final JSONObject j) {
        if (j.getInt("rc") == 0 && j.has("data")) {
            Object data = j.get("data");
            if (data instanceof JSONObject) {
                ((JSONObject) data).remove("chatbotID");
            } else if (data instanceof JSONArray) {
                for (int i = 0; i < ((JSONArray) data).length(); i++) {
                    ((JSONObject) ((JSONArray) data).get(i)).remove("chatbotID");
                }
            }
        }
    }

    /**
     * validate params
     *
     * @param chatbotID
     * @param fromUserId
     * @param textMessage
     */
    private void v(final String chatbotID, final String fromUserId, final String textMessage) throws ChatbotException {
        if (StringUtils.isBlank(chatbotID)) {
            throw new ChatbotException("[conversation] 不合法的聊天机器人标识。");
        }

        if (StringUtils.isBlank(fromUserId)) {
            throw new ChatbotException("[conversation] 不合法的用户标识。");
        }

        if (StringUtils.isBlank(textMessage)) {
            throw new ChatbotException("[conversation] 不合法的消息内容。");
        }
    }

    /**
     * 获得Url的固定前缀
     *
     * @return
     */
    private StringBuffer getUrlPrefix() {
        StringBuffer sb = new StringBuffer();
        return sb.append(this.getBaseUrl());
    }

    /**
     * 获得请求Path的固定前缀
     *
     * @return
     */
    private StringBuffer getPathPrefix() {
        StringBuffer sb = new StringBuffer();
        return sb.append(Constants.basePath);
    }
}
