/*
 * Copyright (C) 2018 Chatopera Inc, <https://www.chatopera.com>
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

import com.chatopera.bot.exception.ChatbotException;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

/**
 * Chatopera聊天机器人
 */
public class Chatbot {

    private String schema;
    private String hostname;
    private int port;
    private String baseUrl;
    private String clientId;
    private String clientSecret;
    private Credentials credentials;

    // 不支持定义空实例
    private Chatbot() {
    }


    /**
     * 创建聊天机器人实例，登录 https://bot.chatopera.com 获得 clientId 和 clientSecret
     * @param clientId
     * @param clientSecret
     * @param baseUrl
     * @throws ChatbotException
     * @throws MalformedURLException
     */
    public Chatbot(final String clientId, final String clientSecret, final String baseUrl) throws ChatbotException, MalformedURLException {
        if (StringUtils.isBlank(baseUrl))
            throw new ChatbotException("智能问答引擎URL不能为空。");

        if (StringUtils.isNotBlank(clientId) && StringUtils.isNotBlank(clientSecret)) {
            this.clientId = clientId;
            this.clientSecret = clientSecret;
            this.credentials = new Credentials(this.clientId, this.clientSecret);
        } else {
            // no credentials is set.
        }

        parseEndpoint(baseUrl);
    }

    /**
     * 创建聊天机器人实例，登录 https://bot.chatopera.com 获得 clientId 和 clientSecret
     * @param clientId
     * @param clientSecret
     * @throws MalformedURLException
     * @throws ChatbotException
     */
    public Chatbot(final String clientId, final String clientSecret) throws MalformedURLException, ChatbotException {
        this(clientId, clientSecret, Constants.defaultBaseUrl);
    }

    /**
     * 分析URL信息
     * @param url
     * @throws MalformedURLException
     */
    private void parseEndpoint(final String url) throws MalformedURLException {
        URL uri = new URL(url);
        this.schema = uri.getProtocol();
        this.hostname = uri.getHost();
        this.port = uri.getPort();

        if (port == -1) {
            this.baseUrl = this.schema + "://" + this.hostname + Constants.basePath;
        } else {
            this.baseUrl = this.schema + "://" + this.hostname + ":" + this.port + Constants.basePath;
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
     * 通过ChatbotID检查一个聊天机器人是否存在
     *
     * @return
     */
    public boolean exists() throws ChatbotException {
        try {
            JSONObject result = this.details();
            int rc = result.getInt("rc");
            if (rc == 0) {
                return true;
            } else if (rc == 3) {
                return false;
            } else {
                throw new ChatbotException("查询聊天机器人异常返回。");
            }
        } catch (Exception e) {
            throw new ChatbotException(e.toString());
        }
    }

    /**
     * 生成认证信息
     * @param method
     * @param path
     * @return
     * @throws Exception
     */
    private HashMap<String, String> auth(final String method, final String path) throws Exception {
        if (this.credentials != null) {
            HashMap<String, String> x = new HashMap<>();
            String token = this.credentials.generate(method, path);
            x.put("Authorization", token);
            return x;
        }
        return null;
    }

    /**
     * 获取聊天机器人详情
     *
     * @return
     * @throws ChatbotException
     */
    public JSONObject details() throws ChatbotException {
        StringBuffer url = new StringBuffer();
        url.append(this.getBaseUrl());
        url.append("/");
        url.append(this.clientId);

        StringBuffer path = new StringBuffer();
        path.append(Constants.basePath);
        path.append("/");
        path.append(this.clientId);
        return request(url.toString(), "GET", path.toString(), null);
    }

    /**
     * 和机器人对话
     * @param userId 用户唯一标识
     * @param textMessage 文字消息
     * @return
     */
    public JSONObject conversation(final String userId, final String textMessage) throws ChatbotException {
        v(this.clientId, userId, textMessage);

        JSONObject body = new JSONObject();
        body.put("fromUserId", userId);
        body.put("textMessage", textMessage);
        body.put("isDebug", false);

        StringBuffer url = new StringBuffer();
        url.append(this.getBaseUrl());
        url.append("/");
        url.append(this.clientId);
        url.append("/conversation/query");

        StringBuffer path = new StringBuffer();
        path.append(Constants.basePath);
        path.append("/");
        path.append(this.clientId);
        path.append("/conversation/query");

        return request(url.toString(), "POST", path.toString(), body);
    }

    /**
     * 意图识别
     * @param customerId 客户ID
     * @param textMessage 文字消息
     * @return
     * @throws ChatbotException
     */
    public JSONObject intent(final String customerId, final String textMessage) throws ChatbotException {
        if (StringUtils.isBlank(this.clientId) || StringUtils.isBlank(customerId) || StringUtils.isBlank(textMessage))
            throw new ChatbotException("参数不合法，不能为空。");

        JSONObject body = new JSONObject();
        body.put("clientId", customerId);
        body.put("query", textMessage);

        StringBuffer url = new StringBuffer();
        url.append(this.getBaseUrl());
        url.append("/");
        url.append(this.clientId);
        url.append("/intent/parse");

        StringBuffer path = new StringBuffer();
        path.append(Constants.basePath);
        path.append("/");
        path.append(this.clientId);
        path.append("/intent/parse");

        return request(url.toString(), "POST", path.toString(), new JSONObject());
    }

    /**
     * 检索知识库
     * @param userId
     * @param textMessage
     * @return
     * @throws ChatbotException
     */
    public JSONObject faq(final String userId, final String textMessage) throws ChatbotException {
        v(this.clientId, userId, textMessage);
        JSONObject body = new JSONObject();
        body.put("fromUserId", userId);
        body.put("query", textMessage);
        body.put("isDebug", false);

        StringBuffer url = new StringBuffer();
        url.append(this.getBaseUrl());
        url.append("/");
        url.append(this.clientId);
        url.append("/faq/query");

        StringBuffer path = new StringBuffer();
        path.append(Constants.basePath);
        path.append("/");
        path.append(this.clientId);
        path.append("/faq/query");

        return request(url.toString(), "POST", path.toString(), body);
    }

    /**
     * 获得聊天机器人用户列表
     * @param page 页面
     * @param pageSize 页面大小
     * @return
     * @throws ChatbotException
     */
    public JSONObject users(int page, int pageSize) throws ChatbotException {
        if (page == 0) {
            page = 1;
        }

        if (pageSize == 0) {
            pageSize = 30;
        }

        StringBuffer url = new StringBuffer();
        url.append(this.getBaseUrl());
        url.append("/");
        url.append(this.clientId);
        url.append("/users?page=");
        url.append(page);
        url.append("&limit=");
        url.append(pageSize);
        url.append("&sortby=-lasttime");

        StringBuffer path = new StringBuffer();
        path.append(Constants.basePath);
        path.append("/");
        path.append(this.clientId);
        path.append("/users?page=");
        path.append(page);
        path.append("&limit=");
        path.append(pageSize);
        path.append("&sortby=-lasttime");

        return request(url.toString(), "GET", path.toString(), null);
    }

    /**
     * 根据用户ID获得聊天历史
     * @param userId 用户唯一标识
     * @param page 页面
     * @param pageSize 页面大小
     * @return
     * @throws ChatbotException
     */
    public JSONObject chats(final String userId, int page, int pageSize) throws ChatbotException {
        if (page == 0) {
            page = 1;
        }

        if (pageSize == 0) {
            pageSize = 30;
        }

        StringBuffer url = new StringBuffer();
        url.append(this.getBaseUrl());
        url.append("/");
        url.append(this.clientId);
        url.append("/users/");
        url.append(userId);
        url.append("/chats?page=");
        url.append(page);
        url.append("&limit=");
        url.append(pageSize);
        url.append("&sortby=-lasttime");

        StringBuffer path = new StringBuffer();
        path.append(Constants.basePath);
        path.append("/");
        path.append(this.clientId);
        path.append("/users/");
        path.append(userId);
        path.append("/chats?page=");
        path.append(page);
        path.append("&limit=");
        path.append(pageSize);
        path.append("&sortby=-lasttime");

        return request(url.toString(), "GET", path.toString(), null);
    }

    /**
     * 根据用户ID屏蔽一个用户
     *
     * @param userId 用户唯一标识
     */
    public void mute(final String userId) throws ChatbotException {
        StringBuffer url = new StringBuffer();
        url.append(this.getBaseUrl());
        url.append("/");
        url.append(this.clientId);
        url.append("/users/");
        url.append(userId);
        url.append("/mute");

        StringBuffer path = new StringBuffer();
        path.append(Constants.basePath);
        path.append("/");
        path.append(this.clientId);
        path.append("/users/");
        path.append(userId);
        path.append("/mute");

        JSONObject response = request(url.toString(), "POST", path.toString(), new JSONObject());
        if (response.getInt("rc") != 0)
            throw new ChatbotException(response.toString());
    }

    /**
     * 根据用户ID取消屏蔽
     * @param userId 用户唯一标识
     * @throws ChatbotException
     */
    public void unmute(final String userId) throws ChatbotException {
        StringBuffer url = new StringBuffer();
        url.append(this.getBaseUrl());
        url.append("/");
        url.append(this.clientId);
        url.append("/users/");
        url.append(userId);
        url.append("/unmute");

        StringBuffer path = new StringBuffer();
        path.append(Constants.basePath);
        path.append("/");
        path.append(this.clientId);
        path.append("/users/");
        path.append(userId);
        path.append("/unmute");

        JSONObject response = request(url.toString(), "POST", path.toString(), new JSONObject());
        if (response.getInt("rc") != 0)
            throw new ChatbotException(response.toString());
    }

    /**
     * 查看一个用户是否被屏蔽
     * @param userId 用户唯一标识
     * @return
     * @throws ChatbotException
     */
    public boolean ismute(final String userId) throws ChatbotException {
        StringBuffer url = new StringBuffer();
        url.append(this.getBaseUrl());
        url.append("/");
        url.append(this.clientId);
        url.append("/users/");
        url.append(userId);
        url.append("/ismute");

        StringBuffer path = new StringBuffer();
        path.append(Constants.basePath);
        path.append("/");
        path.append(this.clientId);
        path.append("/users/");
        path.append(userId);
        path.append("/ismute");

        JSONObject response = request(url.toString(), "POST", path.toString(), new JSONObject());
        System.out.println("ismute " + response.toString());

        if (response.getInt("rc") != 0)
            throw new ChatbotException(response.toString());

        return response.getJSONObject("data").getBoolean("mute");
    }

    private JSONObject request(final String url, final String method, final String path, final JSONObject body) throws ChatbotException {
        JSONObject response;
        try {
            switch (method) {
                case "GET":
                    response = RestAPI.get(url, null, auth("GET", path));
                    break;
                case "POST":
                    response = RestAPI.post(url, body, null, auth("POST", path));
                    break;
                default:
                    throw new ChatbotException("Invalid request method.");
            }
        } catch (Exception e) {
            throw new ChatbotException(e.toString());
        }

        purge(response);
        return response;
    }

    /**
     * remove data
     *
     * @param j
     */
    private void purge(final JSONObject j) {
        if (j.getInt("rc") == 0) {
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
        if (StringUtils.isBlank(chatbotID))
            throw new ChatbotException("[conversation] 不合法的聊天机器人标识。");

        if (StringUtils.isBlank(fromUserId))
            throw new ChatbotException("[conversation] 不合法的用户标识。");

        if (StringUtils.isBlank(textMessage))
            throw new ChatbotException("[conversation] 不合法的消息内容。");
    }
}
