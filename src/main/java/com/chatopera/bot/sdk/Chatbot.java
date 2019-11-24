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

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;

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
     *
     * @param clientId
     * @param clientSecret
     * @param baseUrl
     * @throws ChatbotException
     * @throws MalformedURLException
     */
    public Chatbot(final String clientId, final String clientSecret, final String baseUrl) throws ChatbotException, MalformedURLException {
        if (StringUtils.isBlank(baseUrl)) {
            throw new ChatbotException("智能问答引擎URL不能为空。");
        }

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
     *
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
     *
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
     * @return JSONObject
     * @throws ChatbotException
     */
    public JSONObject details() throws ChatbotException {
        StringBuffer url = getUrlPrefix();
        StringBuffer path = getPathPrefix();
        return request(url.toString(), "GET", path.toString(), null);
    }


    /**
     * 和机器人对话
     *
     * @param userId      用户唯一标识
     * @param textMessage 文字消息
     * @return
     * @throws ChatbotException
     */
    public JSONObject conversation(final String userId, final String textMessage) throws ChatbotException {
        return conversation(userId, textMessage, 0.8, 0.6);
    }

    /**
     * 和机器人对话
     *
     * @param userId
     * @param textMessage
     * @param faqThresholdBestReply
     * @param faqThresholdSuggReply
     * @return
     * @throws ChatbotException
     */
    public JSONObject conversation(final String userId, final String textMessage, final double faqThresholdBestReply, final double faqThresholdSuggReply) throws ChatbotException {
        v(this.clientId, userId, textMessage);

        JSONObject body = new JSONObject();
        body.put("fromUserId", userId);
        body.put("textMessage", textMessage);
        body.put("isDebug", false);
        body.put("faq_best_reply", faqThresholdBestReply);
        body.put("faq_sugg_reply", faqThresholdSuggReply);

        StringBuffer url = getUrlPrefix();
        url.append("/conversation/query");

        StringBuffer path = getPathPrefix();
        path.append("/conversation/query");

        return request(url.toString(), "POST", path.toString(), body);
    }

    /**
     * 检索知识库
     *
     * @param userId      用户唯一标识
     * @param textMessage 文字消息
     * @return
     * @throws ChatbotException
     */
    public JSONObject faq(final String userId, final String textMessage) throws ChatbotException {
        return faq(userId, textMessage, 0.8, 0.6);
    }


    /**
     * 检索知识库
     * @param userId
     * @param textMessage
     * @param faqThresholdBestReply
     * @param faqThresholdSuggReply
     * @return
     * @throws ChatbotException
     */
    public JSONObject faq(final String userId, final String textMessage, final double faqThresholdBestReply, final double faqThresholdSuggReply) throws ChatbotException {
        v(this.clientId, userId, textMessage);
        JSONObject body = new JSONObject();
        body.put("fromUserId", userId);
        body.put("query", textMessage);
        body.put("isDebug", false);
        body.put("faq_best_reply", faqThresholdBestReply);
        body.put("faq_sugg_reply", faqThresholdSuggReply);

        StringBuffer url = getUrlPrefix();
        url.append("/faq/query");

        StringBuffer path = getPathPrefix();
        path.append("/faq/query");

        return request(url.toString(), "POST", path.toString(), body);
    }

    /**
     * 查询知识库列表
     *
     * @param query    查询语句
     * @param category 分类，使用父分类会将子分类的内容同时检索出来
     * @param page     页码，默认为1
     * @param pageSize 每页数据条数，默认 30
     * @return
     * @throws ChatbotException
     * @throws UnsupportedEncodingException
     */
    public JSONObject faqlist(final String query, final String category, int page, int pageSize) throws ChatbotException, UnsupportedEncodingException {
        if (page == 0) {
            page = 1;
        }

        if (pageSize == 0) {
            pageSize = 30;
        }

        StringBuffer url = getUrlPrefix();
        url.append("/faq/database?page=");
        url.append(page);
        url.append("&limit=");
        url.append(pageSize);

        // 检索条件
        if (StringUtils.isNotBlank(query)) {
            url.append("&q=");
            url.append(URLEncoder.encode(query, "UTF-8"));
        }

        // 分类
        if (StringUtils.isNotBlank(category)) {
            url.append("&category=");
            url.append(category);
        }

        StringBuffer path = getPathPrefix();
        path.append("/faq/database?page=");
        path.append(page);
        path.append("&limit=");
        path.append(pageSize);

        // 检索条件
        if (StringUtils.isNotBlank(query)) {
            path.append("&q=");
            path.append(URLEncoder.encode(query, "UTF-8"));
        }

        // 分类
        if (StringUtils.isNotBlank(category)) {
            path.append("&category=");
            path.append(category);
        }

        return request(url.toString(), "GET", path.toString(), null);
    }

    /**
     * 创建知识库的问答对
     *
     * @param post       标准问的问题
     * @param reply      标准问的回答
     * @param enabled    是否启用，默认为 false
     * @param categories 类别，是分类标识的数组，并不支持分类名称，必须使用分类标识
     * @return
     * @throws ChatbotException
     */
    public JSONObject faqcreate(final String post, final String reply, boolean enabled, final List<String> categories) throws ChatbotException {
        if (StringUtils.isBlank(post) || StringUtils.isBlank(reply)) {
            throw new ChatbotException("Invalid post or reply");
        }

        StringBuffer url = getUrlPrefix();
        url.append("/faq/database");

        StringBuffer path = getPathPrefix();
        path.append("/faq/database");


        JSONObject body = new JSONObject();
        body.put("post", post);
        body.put("reply", reply);

        if (enabled) {
            body.put("enabled", true);
        } else {
            body.put("enabled", false);
        }

        if ((categories != null) && categories.size() > 0) {
            JSONArray ja = new JSONArray();
            for (int i = 0; i < categories.size(); i++) {
                ja.put(categories.get(i));
            }
            body.put("categories", ja);
        } else if ((categories != null) && categories.size() == 0) {
            // 设置为全部
            body.put("categories", new JSONArray());
        }

        return request(url.toString(), "POST", path.toString(), body);
    }


    /**
     * 获得知识库一条记录的详情
     *
     * @param id 问答对唯一标识
     * @return
     * @throws ChatbotException
     */
    public JSONObject faqdetail(final String id) throws ChatbotException {
        if (StringUtils.isBlank(id)) {
            throw new ChatbotException("Invalid id");
        }

        StringBuffer url = getUrlPrefix();
        url.append("/faq/database/");
        url.append(id);

        StringBuffer path = getPathPrefix();
        path.append("/faq/database/");
        path.append(id);

        return request(url.toString(), "GET", path.toString(), null);
    }

    /**
     * 更新知识库一条记录
     *
     * @param id         问答对唯一标识
     * @param post       问题
     * @param reply      回复
     * @param enabled    是否启用
     * @param categories 类别，是分类标识的数组，并不支持分类名称，必须使用分类标识
     * @return JSONObject
     * @throws ChatbotException
     */
    public JSONObject faqupdate(final String id, final String post, final String reply, boolean enabled, final List<String> categories) throws ChatbotException {
        if (StringUtils.isBlank(id)) {
            throw new ChatbotException("Invalid id");
        }

        StringBuffer url = getUrlPrefix();
        url.append("/faq/database/");
        url.append(id);

        StringBuffer path = getPathPrefix();
        path.append("/faq/database/");
        path.append(id);

        JSONObject obj = new JSONObject();
        if (StringUtils.isNotBlank(post)) {
            obj.put("post", post);
        }

        if (StringUtils.isNotBlank(reply)) {
            obj.put("reply", reply);
        }

        if (enabled) {
            obj.put("enabled", true);
        } else {
            obj.put("enabled", false);
        }

        if ((categories != null) && categories.size() > 0) {
            JSONArray ja = new JSONArray();
            for (int i = 0; i < categories.size(); i++) {
                ja.put(categories.get(i));
            }
            obj.put("categories", ja);
        } else if ((categories != null) && categories.size() == 0) {
            // 设置为全部
            obj.put("categories", new JSONArray());
        }

        return request(url.toString(), "PUT", path.toString(), obj);
    }

    /**
     * 禁用知识库里一个问答对
     *
     * @param id 问答对唯一标识
     * @return JSONObject
     * @throws ChatbotException
     */
    public JSONObject faqdisable(final String id) throws ChatbotException {
        if (StringUtils.isBlank(id)) {
            throw new ChatbotException("Invalid id");
        }
        return this.faqupdate(id, null, null, false, null);
    }


    /**
     * 启用知识库里一个问答对
     *
     * @param id 问答对唯一标识
     * @return JSONObject
     * @throws ChatbotException
     */
    public JSONObject faqenable(final String id) throws ChatbotException {
        if (StringUtils.isBlank(id)) {
            throw new ChatbotException("Invalid id");
        }
        return this.faqupdate(id, null, null, true, null);
    }

    /**
     * 删除知识库的一条记录，标准问和扩展问都被物理性删除
     *
     * @param id 问答对唯一标识
     * @return
     * @throws ChatbotException
     */
    public JSONObject faqdelete(final String id) throws ChatbotException {
        if (StringUtils.isBlank(id)) {
            throw new ChatbotException("Invalid id");
        }

        StringBuffer url = getUrlPrefix();
        url.append("/faq/database/");
        url.append(id);

        StringBuffer path = getPathPrefix();
        path.append("/faq/database/");
        path.append(id);

        return request(url.toString(), "DELETE", path.toString(), null);
    }


    /**
     * 查询知识库标准问的扩展问列表
     *
     * @param id 问答对唯一标识
     * @return
     * @throws ChatbotException
     */
    public JSONObject faqextend(final String id) throws ChatbotException {
        if (StringUtils.isBlank(id)) {
            throw new ChatbotException("Invalid id");
        }

        StringBuffer url = getUrlPrefix();
        url.append("/faq/database/");
        url.append(id);
        url.append("/extend");

        StringBuffer path = getPathPrefix();
        path.append("/faq/database/");
        path.append(id);
        path.append("/extend");

        return request(url.toString(), "GET", path.toString(), null);
    }

    /**
     * 创建知识库标准问的扩展问
     *
     * @param id   问答对唯一标识
     * @param post 扩展问
     * @return JSONObject
     */
    public JSONObject faqextendcreate(final String id, final String post) throws ChatbotException {
        if (StringUtils.isBlank(id) || StringUtils.isBlank(post)) {
            throw new ChatbotException("Invalid id or post");
        }

        StringBuffer url = getUrlPrefix();
        url.append("/faq/database/");
        url.append(id);
        url.append("/extend");

        StringBuffer path = getPathPrefix();
        path.append("/faq/database/");
        path.append(id);
        path.append("/extend");

        JSONObject body = new JSONObject();
        body.put("post", post);

        return request(url.toString(), "POST", path.toString(), body);
    }

    /**
     * 更新知识库标准问的扩展问
     *
     * @param id   问答对唯一标识
     * @param post 扩展问
     * @return JSONObject
     */
    public JSONObject faqextendupdate(final String id, final String extendId, final String post) throws ChatbotException {
        if (StringUtils.isBlank(id) || StringUtils.isBlank(post) || StringUtils.isBlank(extendId)) {
            throw new ChatbotException("Invalid id, post or extendId");
        }

        StringBuffer url = getUrlPrefix();
        url.append("/faq/database/");
        url.append(id);
        url.append("/extend/");
        url.append(extendId);

        StringBuffer path = getPathPrefix();
        path.append("/faq/database/");
        path.append(id);
        path.append("/extend/");
        path.append(extendId);

        JSONObject body = new JSONObject();
        body.put("post", post);

        return request(url.toString(), "PUT", path.toString(), body);
    }


    /**
     * 删除知识库标准问的扩展问
     *
     * @param id       问答对唯一标识
     * @param extendId 扩展问唯一标识
     * @return
     * @throws ChatbotException
     */
    public JSONObject faqextenddelete(final String id, final String extendId) throws ChatbotException {
        if (StringUtils.isBlank(id) || StringUtils.isBlank(extendId)) {
            throw new ChatbotException("Invalid id or extendId");
        }

        StringBuffer url = getUrlPrefix();
        url.append("/faq/database/");
        url.append(id);
        url.append("/extend/");
        url.append(extendId);

        StringBuffer path = getPathPrefix();
        path.append("/faq/database/");
        path.append(id);
        path.append("/extend/");
        path.append(extendId);

        return request(url.toString(), "DELETE", path.toString(), null);
    }


    /**
     * 获得知识库分类信息
     *
     * @return
     * @throws ChatbotException
     */
    public JSONObject faqcategories() throws ChatbotException {
        StringBuffer url = getUrlPrefix();
        url.append("/faq/categories");

        StringBuffer path = getPathPrefix();
        path.append("/faq/categories");

        return request(url.toString(), "GET", path.toString(), null);
    }

    /**
     * 创建知识库分类
     *
     * @param label    分类名称
     * @param parentId 父节点的标识， 即上级分类的标识
     * @return
     * @throws ChatbotException
     */
    public JSONObject faqcategorycreate(final String label, final String parentId) throws ChatbotException {
        if (StringUtils.isBlank(label)) {
            throw new ChatbotException("Invalid label");
        }

        StringBuffer url = getUrlPrefix();
        url.append("/faq/categories");

        StringBuffer path = getPathPrefix();
        path.append("/faq/categories");

        JSONObject body = new JSONObject();
        body.put("label", label);

        if (StringUtils.isNotBlank(parentId)) {
            body.put("parentId", parentId);
        }

        return request(url.toString(), "POST", path.toString(), body);
    }


    /**
     * 更新知识库分类
     *
     * @param value 知识库分类的标识
     * @param label 知识库分类名称
     * @return
     * @throws ChatbotException
     */
    public JSONObject faqcategoryupdate(final String value, final String label) throws ChatbotException {
        if (StringUtils.isBlank(label) || StringUtils.isBlank(value)) {
            throw new ChatbotException("Invalid label or value");
        }

        StringBuffer url = getUrlPrefix();
        url.append("/faq/categories");

        StringBuffer path = getPathPrefix();
        path.append("/faq/categories");

        JSONObject body = new JSONObject();
        body.put("label", label);
        body.put("value", value);

        return request(url.toString(), "PUT", path.toString(), body);
    }


    /**
     * 删除知识库分类
     *
     * @param value 知识库分类的标识
     * @return
     * @throws ChatbotException
     */
    public JSONObject faqcategorydelete(final String value) throws ChatbotException {
        if (StringUtils.isBlank(value)) {
            throw new ChatbotException("Invalid value");
        }

        StringBuffer url = getUrlPrefix();
        url.append("/faq/categories/");
        url.append(value);

        StringBuffer path = getPathPrefix();
        path.append("/faq/categories/");
        path.append(value);

        return request(url.toString(), "DELETE", path.toString(), null);
    }


    /**
     * 创建意图识别会话
     * 此处支持请求生产版本
     *
     * @param userId  用户唯一标识
     * @param channel 渠道标识，代表不同渠道的唯一标识，比如QQ，公众号，开发者自定义
     * @return
     * @throws ChatbotException
     */
    public JSONObject intentsession(final String userId, String channel) throws ChatbotException {
        JSONObject body = new JSONObject();
        body.put("uid", userId);
        body.put("channel", channel);

        StringBuffer url = getUrlPrefix();
        url.append("/clause/prover/session");

        StringBuffer path = getPathPrefix();
        path.append("/clause/prover/session");

        return request(url.toString(), "POST", path.toString(), body);
    }

    /**
     * 获得指定意图识别会话ID的详情
     *
     * @param sessionId
     * @return
     * @throws ChatbotException
     */
    public JSONObject intentsession(final String sessionId) throws ChatbotException {

        StringBuffer url = getUrlPrefix();
        url.append("/clause/prover/session/");
        url.append(sessionId);

        StringBuffer path = getPathPrefix();
        path.append("/clause/prover/session/");
        path.append(sessionId);

        return request(url.toString(), "GET", path.toString(), null);
    }


    /**
     * 进行意图识别对话
     *
     * @param sessionId   会话ID
     * @param userId      用户唯一标识，需要和创建会话时保持一致，否则会话会错乱
     * @param textMessage 消息文本内容
     * @return
     * @throws ChatbotException
     */
    public JSONObject intent(final String sessionId, final String userId, final String textMessage) throws ChatbotException {
        if (StringUtils.isBlank(sessionId)) {
            throw new ChatbotException("[intent] 不合法的会话ID。");
        }

        if (StringUtils.isBlank(userId)) {
            throw new ChatbotException("[intent] 不合法的用户标识。");
        }

        if (StringUtils.isBlank(textMessage)) {
            throw new ChatbotException("[intent] 不合法的消息内容。");
        }

        JSONObject body = new JSONObject();
        body.put("fromUserId", userId);
        JSONObject session = new JSONObject();
        session.put("id", sessionId);
        JSONObject message = new JSONObject();
        message.put("textMessage", textMessage);
        body.put("session", session);
        body.put("message", message);

        StringBuffer url = getUrlPrefix();
        url.append("/clause/prover/chat");

        StringBuffer path = getPathPrefix();
        path.append("/clause/prover/chat");

        return request(url.toString(), "POST", path.toString(), body);
    }


    /**
     * 获得聊天机器人用户列表
     *
     * @param page     页面
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

        StringBuffer url = getUrlPrefix();
        url.append("/users?page=");
        url.append(page);
        url.append("&limit=");
        url.append(pageSize);
        url.append("&sortby=-lasttime");

        StringBuffer path = getPathPrefix();
        path.append("/users?page=");
        path.append(page);
        path.append("&limit=");
        path.append(pageSize);
        path.append("&sortby=-lasttime");

        return request(url.toString(), "GET", path.toString(), null);
    }


    /**
     * 根据用户ID获得聊天历史
     *
     * @param userId   用户唯一标识
     * @param page     页面
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

        StringBuffer url = getUrlPrefix();
        url.append("/users/");
        url.append(userId);
        url.append("/chats?page=");
        url.append(page);
        url.append("&limit=");
        url.append(pageSize);
        url.append("&sortby=-lasttime");

        StringBuffer path = getPathPrefix();
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
        StringBuffer url = getUrlPrefix();
        url.append("/users/");
        url.append(userId);
        url.append("/mute");

        StringBuffer path = getPathPrefix();
        path.append("/users/");
        path.append(userId);
        path.append("/mute");

        JSONObject response = request(url.toString(), "POST", path.toString(), new JSONObject());
        if (response.getInt("rc") != 0) {
            throw new ChatbotException(response.toString());
        }
    }

    /**
     * 根据用户ID取消屏蔽
     *
     * @param userId 用户唯一标识
     * @throws ChatbotException
     */
    public void unmute(final String userId) throws ChatbotException {
        StringBuffer url = getUrlPrefix();
        url.append("/users/");
        url.append(userId);
        url.append("/unmute");

        StringBuffer path = getPathPrefix();
        path.append("/users/");
        path.append(userId);
        path.append("/unmute");

        JSONObject response = request(url.toString(), "POST", path.toString(), new JSONObject());
        if (response.getInt("rc") != 0) {
            throw new ChatbotException(response.toString());
        }
    }

    /**
     * 查看一个用户是否被屏蔽
     *
     * @param userId 用户唯一标识
     * @return
     * @throws ChatbotException
     */
    public boolean ismute(final String userId) throws ChatbotException {
        StringBuffer url = getUrlPrefix();
        url.append("/users/");
        url.append(userId);
        url.append("/ismute");

        StringBuffer path = getPathPrefix();
        path.append("/users/");
        path.append(userId);
        path.append("/ismute");

        JSONObject response = request(url.toString(), "POST", path.toString(), new JSONObject());
        System.out.println("ismute " + response.toString());

        if (response.getInt("rc") != 0) {
            throw new ChatbotException(response.toString());
        }

        return response.getJSONObject("data").getBoolean("mute");
    }

    private JSONObject request(final String url, final String method, final String path, final JSONObject body) throws ChatbotException {
        System.out.println("[request] url: " + url + ", method: " + method + ", path: " + path);
        JSONObject response;
        try {
            switch (method) {
                case "GET":
                    response = RestAPI.get(url, null, auth(method, path));
                    break;
                case "POST":
                    response = RestAPI.post(url, body, null, auth(method, path));
                    break;
                case "DELETE":
                    response = RestAPI.delete(url, auth(method, path));
                    break;
                case "PUT":
                    response = RestAPI.put(url, body, null, auth(method, path));
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
        return sb.append(this.getBaseUrl()).append("/").append(this.clientId);
    }

    /**
     * 获得请求Path的固定前缀
     *
     * @return
     */
    private StringBuffer getPathPrefix() {
        StringBuffer sb = new StringBuffer();
        return sb.append(Constants.basePath).append("/").append(this.clientId);
    }


}
