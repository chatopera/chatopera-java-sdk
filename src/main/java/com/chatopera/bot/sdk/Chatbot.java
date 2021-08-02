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
package com.chatopera.bot.sdk;

import com.chatopera.bot.exception.ChatbotException;
import com.chatopera.bot.utils.FileUtil;
import kong.unirest.Unirest;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

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
    private final static int ASR_DEFAULT_NBEST = 5;
    private final static boolean ASR_DEFAULT_POS = false;

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
     * 生成认证信息
     *
     * @param method
     * @param path
     * @return
     * @throws Exception
     */
    private HashMap<String, String> auth(final String method, final String path) throws Exception {
        HashMap<String, String> x = new HashMap<>();
        if (this.credentials != null) {
            String token = this.credentials.generate(method, path);
            x.put("Authorization", token);
        }
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
        StringBuffer fullPath = getPathPrefix();

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
            fullPath.append(path);
        }

        /**
         * 发送请求
         */
        JSONObject result;
        try {
            switch (method) {
                case "GET":
                    result = RestAPI.get(url.toString(), null, auth(method, fullPath.toString()));
                    break;
                case "POST":
                    if (StringUtils.startsWith(path, "/asr/recognize")) {
                        Optional<JSONObject> resultOpt = postAsrRecognize(url.toString(), payload, null, auth(method, fullPath.toString()));
                        if (resultOpt.isPresent()) {
                            result = resultOpt.get();
                        } else {
                            throw new ChatbotException("Empty response from ASR Api.");
                        }
                    } else {
                        result = RestAPI.post(url.toString(), payload, null, auth(method, fullPath.toString()));
                    }
                    break;
                case "DELETE":
                    result = RestAPI.delete(url.toString(), auth(method, fullPath.toString()));
                    break;
                case "PUT":
                    result = RestAPI.put(url.toString(), payload, null, auth(method, fullPath.toString()));
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
     * 语音识别 API
     *
     * @param url
     * @param payload
     * @param query
     * @param headers
     * @return
     */
    private Optional<JSONObject> postAsrRecognize(final String url, final JSONObject payload, final String query, final HashMap<String, String> headers) throws ChatbotException {
        if (payload.has("filepath") && FileUtil.exists(payload.getString("filepath"))) {
            kong.unirest.json.JSONObject obj = Unirest.post(url)
                    .header("Authorization", headers.containsKey("Authorization") ? headers.get("Authorization") : "")
                    .field("file", new File(payload.getString("filepath")))
                    .field("nbest", Integer.toString((payload.has("nbest") ? payload.getInt("nbest") : ASR_DEFAULT_NBEST)))
                    .field("pos", Boolean.toString(payload.has("pos") ? payload.getBoolean("pos") : ASR_DEFAULT_POS))
                    .asJson().getBody().getObject();

            return Optional.of(new JSONObject(obj.toString()));
        } else if (payload.has("type") && StringUtils.equalsIgnoreCase(payload.getString("type"), "base64")) {
            if (payload.has("data")) {
                String data = payload.getString("data");
                if (StringUtils.isNotBlank(data)) {
                    // 使用 base64 格式请求
                    if (!payload.has("nbest"))
                        payload.put("nbest", ASR_DEFAULT_NBEST);

                    if (!payload.has("pos"))
                        payload.put("pos", ASR_DEFAULT_POS);

                    return Optional.of(RestAPI.post(url, payload, null, headers));
                } else {
                    throw new ChatbotException("Empty data for ASR Api, base64 data is required for base64 type request.");
                }
            } else {
                throw new ChatbotException("`data` is required for base64 type request body.");
            }
        } else {
            throw new ChatbotException("Invalid body for ASR Api, filepath or `type=base64, data=xxx` are required.");
        }
    }

    /**
     * 核心访问接口
     *
     * @param method PUT, POST, etc.
     * @param path   /faq/xxxx
     * @return
     * @throws ChatbotException
     */
    public Response command(final String method, final String path) throws ChatbotException {
        return command(method, path, null);
    }


    /**
     * 获取聊天机器人详情
     *
     * @return JSONObject
     * @throws ChatbotException
     * @deprecated use `Chatbot#command` API instead, removed in 2020-10
     */
    public JSONObject details() throws ChatbotException {
        Response resp = command("GET", "/");
        return resp.toJSON();
    }


    /**
     * 通过ChatbotID检查一个聊天机器人是否存在
     *
     * @return
     * @throws ChatbotException
     * @deprecated use `Chatbot#command` API instead, removed in 2020-10
     */
    public boolean exists() throws ChatbotException {
        try {
            Response resp = command("GET", "/");
            if (resp.getRc() == 0) {
                return true;
            } else if (resp.getRc() == 3) {
                return false;
            } else {
                throw new ChatbotException("查询聊天机器人异常返回。");
            }
        } catch (Exception e) {
            throw new ChatbotException(e.toString());
        }
    }

    /**
     * 和机器人对话
     *
     * @param userId      用户唯一标识
     * @param textMessage 文字消息
     * @return
     * @throws ChatbotException
     * @deprecated use `Chatbot#command` API instead, removed in 2020-10
     */
    public JSONObject conversation(final String userId, final String textMessage) throws ChatbotException {
        return conversation(userId, textMessage, 0.8, 0.6);
    }


    /**
     * 和机器人对话
     *
     * @param userId
     * @param textMessage
     * @param faqBestReplyThreshold
     * @param faqSuggReplyThreshold
     * @return
     * @throws ChatbotException
     * @deprecated use `Chatbot#command` API instead, removed in 2020-10
     */
    public JSONObject conversation(final String userId, final String textMessage, final double faqBestReplyThreshold, final double faqSuggReplyThreshold) throws ChatbotException {
        v(this.clientId, userId, textMessage);

        JSONObject body = new JSONObject();
        body.put("fromUserId", userId);
        body.put("textMessage", textMessage);
        body.put("isDebug", false);
        body.put("faqBestReplyThreshold", faqBestReplyThreshold);
        body.put("faqSuggReplyThreshold", faqSuggReplyThreshold);

        Response resp = command("POST", "/conversation/query", body);
        return resp.toJSON();
    }

    /**
     * 检索知识库
     *
     * @param userId      用户唯一标识
     * @param textMessage 文字消息
     * @return
     * @throws ChatbotException
     * @deprecated use `Chatbot#command` API instead, removed in 2020-10
     */
    public JSONObject faq(final String userId, final String textMessage) throws ChatbotException {
        return faq(userId, textMessage, 0.8, 0.6);
    }


    /**
     * 检索知识库
     *
     * @param userId
     * @param textMessage
     * @param faqBestReplyThreshold
     * @param faqSuggReplyThreshold
     * @return
     * @throws ChatbotException
     * @deprecated use `Chatbot#command` API instead, removed in 2020-10
     */
    public JSONObject faq(final String userId, final String textMessage, final double faqBestReplyThreshold, final double faqSuggReplyThreshold) throws ChatbotException {
        v(this.clientId, userId, textMessage);
        JSONObject body = new JSONObject();
        body.put("fromUserId", userId);
        body.put("query", textMessage);
        body.put("isDebug", false);
        body.put("faqBestReplyThreshold", faqBestReplyThreshold);
        body.put("faqSuggReplyThreshold", faqSuggReplyThreshold);

        Response resp = command("POST", "/faq/query", body);

        return resp.toJSON();
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
     * @deprecated use `Chatbot#command` API instead, removed in 2020-10
     */
    public JSONObject faqlist(final String query, final String category, int page, int pageSize) throws ChatbotException, UnsupportedEncodingException, UnsupportedEncodingException {
        if (page == 0) {
            page = 1;
        }

        if (pageSize == 0) {
            pageSize = 30;
        }

        StringBuffer path = new StringBuffer();
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

        Response resp = command("GET", path.toString());
        return resp.toJSON();
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
     * @deprecated use `Chatbot#command` API instead, removed in 2020-10
     */
    public JSONObject faqcreate(final String post, final String reply, boolean enabled, final List<String> categories) throws ChatbotException {
        if (StringUtils.isBlank(post) || StringUtils.isBlank(reply)) {
            throw new ChatbotException("Invalid post or reply");
        }

        JSONObject body = new JSONObject();
        body.put("post", post);

        // 支持多答案
        JSONArray replies = new JSONArray();
        JSONObject replyObj = new JSONObject();
        replyObj.put("content", reply);
        replyObj.put("rtype", "plain");
        replyObj.put("enabled", true);
        replies.put(replyObj);

        body.put("replies", replies);

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

        Response resp = command("POST", "/faq/database", body);

        return resp.toJSON();
    }


    /**
     * 获得知识库一条记录的详情
     *
     * @param id 问答对唯一标识
     * @return
     * @throws ChatbotException
     * @deprecated use `Chatbot#command` API instead, removed in 2020-10
     */
    public JSONObject faqdetail(final String id) throws ChatbotException {
        if (StringUtils.isBlank(id)) {
            throw new ChatbotException("Invalid id");
        }

        StringBuffer path = new StringBuffer();
        path.append("/faq/database/");
        path.append(id);

        Response resp = command("GET", path.toString());
        return resp.toJSON();
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
     * @deprecated use `Chatbot#command` API instead, removed in 2020-10
     */
    public JSONObject faqupdate(final String id, final String post, final String reply, boolean enabled, final List<String> categories) throws ChatbotException {
        if (StringUtils.isBlank(id)) {
            throw new ChatbotException("Invalid id");
        }

        // 获得replyLastUpdate
        StringBuffer p = new StringBuffer();
        p.append("/faq/database/");
        p.append(id);

        Response prev = command("GET", p.toString());
        String replyLastUpdate = ((JSONObject) (prev.getData())).getString("replyLastUpdate");

        StringBuffer path = new StringBuffer();
        path.append("/faq/database/");
        path.append(id);

        JSONObject obj = new JSONObject();
        obj.put("replyLastUpdate", replyLastUpdate);
        if (StringUtils.isNotBlank(post)) {
            obj.put("post", post);
        }

        if (StringUtils.isNotBlank(reply)) {
            JSONArray replies = new JSONArray();
            JSONObject replyObj = new JSONObject();
            replyObj.put("content", reply);
            replyObj.put("rtype", "plain");
            replyObj.put("enabled", true);
            replies.put(replyObj);
            obj.put("replies", replies);
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

        Response resp = command("PUT", path.toString(), obj);
        return resp.toJSON();
    }

    /**
     * 禁用知识库里一个问答对
     *
     * @param id 问答对唯一标识
     * @return JSONObject
     * @throws ChatbotException
     * @deprecated use `Chatbot#command` API instead, removed in 2020-10
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
     * @deprecated use `Chatbot#command` API instead, removed in 2020-10
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
     * @deprecated use `Chatbot#command` API instead, removed in 2020-10
     */
    public JSONObject faqdelete(final String id) throws ChatbotException {
        if (StringUtils.isBlank(id)) {
            throw new ChatbotException("Invalid id");
        }

        StringBuffer path = new StringBuffer();
        path.append("/faq/database/");
        path.append(id);

        Response resp = command("DELETE", path.toString());
        return resp.toJSON();
    }


    /**
     * 查询知识库标准问的扩展问列表
     *
     * @param id 问答对唯一标识
     * @return
     * @throws ChatbotException
     * @deprecated use `Chatbot#command` API instead, removed in 2020-10
     */
    public JSONObject faqextend(final String id) throws ChatbotException {
        if (StringUtils.isBlank(id)) {
            throw new ChatbotException("Invalid id");
        }

        StringBuffer path = new StringBuffer();
        path.append("/faq/database/");
        path.append(id);
        path.append("/extend");

        Response resp = command("GET", path.toString());
        return resp.toJSON();
    }

    /**
     * 创建知识库标准问的扩展问
     *
     * @param id   问答对唯一标识
     * @param post 扩展问
     * @return JSONObject
     * @deprecated use `Chatbot#command` API instead, removed in 2020-10
     */
    public JSONObject faqextendcreate(final String id, final String post) throws ChatbotException {
        if (StringUtils.isBlank(id) || StringUtils.isBlank(post)) {
            throw new ChatbotException("Invalid id or post");
        }

        StringBuffer path = new StringBuffer();
        path.append("/faq/database/");
        path.append(id);
        path.append("/extend");

        JSONObject body = new JSONObject();
        body.put("post", post);

        Response resp = command("POST", path.toString(), body);
        return resp.toJSON();
    }

    /**
     * 更新知识库标准问的扩展问
     *
     * @param id   问答对唯一标识
     * @param post 扩展问
     * @return JSONObject
     * @deprecated use `Chatbot#command` API instead, removed in 2020-10
     */
    public JSONObject faqextendupdate(final String id, final String extendId, final String post) throws ChatbotException {
        if (StringUtils.isBlank(id) || StringUtils.isBlank(post) || StringUtils.isBlank(extendId)) {
            throw new ChatbotException("Invalid id, post or extendId");
        }

        StringBuffer path = new StringBuffer();
        path.append("/faq/database/");
        path.append(id);
        path.append("/extend/");
        path.append(extendId);

        JSONObject body = new JSONObject();
        body.put("post", post);

        Response resp = command("PUT", path.toString(), body);
        return resp.toJSON();
    }


    /**
     * 删除知识库标准问的扩展问
     *
     * @param id       问答对唯一标识
     * @param extendId 扩展问唯一标识
     * @return
     * @throws ChatbotException
     * @deprecated use `Chatbot#command` API instead, removed in 2020-10
     */
    public JSONObject faqextenddelete(final String id, final String extendId) throws ChatbotException {
        if (StringUtils.isBlank(id) || StringUtils.isBlank(extendId)) {
            throw new ChatbotException("Invalid id or extendId");
        }

        StringBuffer path = new StringBuffer();
        path.append("/faq/database/");
        path.append(id);
        path.append("/extend/");
        path.append(extendId);

        Response resp = command("DELETE", path.toString());
        return resp.toJSON();
    }


    /**
     * 获得知识库分类信息
     *
     * @return
     * @throws ChatbotException
     * @deprecated use `Chatbot#command` API instead, removed in 2020-10
     */
    public JSONObject faqcategories() throws ChatbotException {
        StringBuffer path = new StringBuffer();
        path.append("/faq/categories");

        Response resp = command("GET", path.toString());
        return resp.toJSON();
    }

    /**
     * 创建知识库分类
     *
     * @param label    分类名称
     * @param parentId 父节点的标识， 即上级分类的标识
     * @return
     * @throws ChatbotException
     * @deprecated use `Chatbot#command` API instead, removed in 2020-10
     */
    public JSONObject faqcategorycreate(final String label, final String parentId) throws ChatbotException {
        if (StringUtils.isBlank(label)) {
            throw new ChatbotException("Invalid label");
        }

        StringBuffer path = getPathPrefix();
        path.append("/faq/categories");

        JSONObject body = new JSONObject();
        body.put("label", label);

        if (StringUtils.isNotBlank(parentId)) {
            body.put("parentId", parentId);
        }

        Response resp = command("POST", path.toString(), body);
        return resp.toJSON();
    }


    /**
     * 更新知识库分类
     *
     * @param value 知识库分类的标识
     * @param label 知识库分类名称
     * @return
     * @throws ChatbotException
     * @deprecated use `Chatbot#command` API instead, removed in 2020-10
     */
    public JSONObject faqcategoryupdate(final String value, final String label) throws ChatbotException {
        if (StringUtils.isBlank(label) || StringUtils.isBlank(value)) {
            throw new ChatbotException("Invalid label or value");
        }
        StringBuffer path = new StringBuffer();
        path.append("/faq/categories");

        JSONObject body = new JSONObject();
        body.put("label", label);
        body.put("value", value);

        Response resp = command("PUT", path.toString(), body);
        return resp.toJSON();
    }


    /**
     * 删除知识库分类
     *
     * @param value 知识库分类的标识
     * @return
     * @throws ChatbotException
     * @deprecated use `Chatbot#command` API instead, removed in 2020-10
     */
    public JSONObject faqcategorydelete(final String value) throws ChatbotException {
        if (StringUtils.isBlank(value)) {
            throw new ChatbotException("Invalid value");
        }

        StringBuffer path = new StringBuffer();
        path.append("/faq/categories/");
        path.append(value);

        Response resp = command("DELETE", path.toString());
        return resp.toJSON();
    }


    /**
     * 创建意图识别会话
     * 此处支持请求生产版本
     *
     * @param userId  用户唯一标识
     * @param channel 渠道标识，代表不同渠道的唯一标识，比如QQ，公众号，开发者自定义
     * @return
     * @throws ChatbotException
     * @deprecated use `Chatbot#command` API instead, removed in 2020-10
     */
    public JSONObject intentsession(final String userId, String channel) throws ChatbotException {
        JSONObject body = new JSONObject();
        body.put("uid", userId);
        body.put("channel", channel);

        StringBuffer path = getPathPrefix();
        path.append("/clause/prover/session");

        Response resp = command("POST", path.toString(), body);
        return resp.toJSON();
    }

    /**
     * 获得指定意图识别会话ID的详情
     *
     * @param sessionId
     * @return
     * @throws ChatbotException
     * @deprecated use `Chatbot#command` API instead, removed in 2020-10
     */
    public JSONObject intentsession(final String sessionId) throws ChatbotException {

        StringBuffer path = new StringBuffer();
        path.append("/clause/prover/session/");
        path.append(sessionId);

        Response resp = command("GET", path.toString());
        return resp.toJSON();
    }


    /**
     * 进行意图识别对话
     *
     * @param sessionId   会话ID
     * @param userId      用户唯一标识，需要和创建会话时保持一致，否则会话会错乱
     * @param textMessage 消息文本内容
     * @return
     * @throws ChatbotException
     * @deprecated use `Chatbot#command` API instead, removed in 2020-10
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

        StringBuffer path = new StringBuffer();
        path.append("/clause/prover/chat");

        Response resp = command("POST", path.toString(), body);
        return resp.toJSON();
    }


    /**
     * 获得聊天机器人用户列表
     *
     * @param page     页面
     * @param pageSize 页面大小
     * @return
     * @throws ChatbotException
     * @deprecated use `Chatbot#command` API instead, removed in 2020-10
     */
    public JSONObject users(int page, int pageSize) throws ChatbotException {
        if (page == 0) {
            page = 1;
        }

        if (pageSize == 0) {
            pageSize = 30;
        }

        StringBuffer path = new StringBuffer();
        path.append("/users?page=");
        path.append(page);
        path.append("&limit=");
        path.append(pageSize);
        path.append("&sortby=-lasttime");

        Response resp = command("GET", path.toString(), null);
        return resp.toJSON();
    }


    /**
     * 根据用户ID获得聊天历史
     *
     * @param userId   用户唯一标识
     * @param page     页面
     * @param pageSize 页面大小
     * @return
     * @throws ChatbotException
     * @deprecated use `Chatbot#command` API instead, removed in 2020-10
     */
    public JSONObject chats(final String userId, int page, int pageSize) throws ChatbotException {
        if (page == 0) {
            page = 1;
        }

        if (pageSize == 0) {
            pageSize = 30;
        }

        StringBuffer path = getPathPrefix();
        path.append("/users/");
        path.append(userId);
        path.append("/chats?page=");
        path.append(page);
        path.append("&limit=");
        path.append(pageSize);
        path.append("&sortby=-lasttime");

        Response resp = command("GET", path.toString());
        return resp.toJSON();
    }

    /**
     * 根据用户ID屏蔽一个用户
     *
     * @param userId 用户唯一标识
     * @deprecated use `Chatbot#command` API instead, removed in 2020-10
     */
    public void mute(final String userId) throws ChatbotException {
        StringBuffer path = new StringBuffer();
        path.append("/users/");
        path.append(userId);
        path.append("/mute");

        Response resp = command("POST", path.toString());

        if (resp.getRc() != 0) {
            throw new ChatbotException("Unable to mute user, bad response from server.");
        }
    }

    /**
     * 根据用户ID取消屏蔽
     *
     * @param userId 用户唯一标识
     * @throws ChatbotException
     * @deprecated use `Chatbot#command` API instead, removed in 2020-10
     */
    public void unmute(final String userId) throws ChatbotException {
        StringBuffer path = new StringBuffer();
        path.append("/users/");
        path.append(userId);
        path.append("/unmute");

        Response resp = command("POST", path.toString());

        if (resp.getRc() != 0) {
            throw new ChatbotException("Unable to unmute user, bad response from server.");
        }
    }

    /**
     * 查看一个用户是否被屏蔽
     *
     * @param userId 用户唯一标识
     * @return
     * @throws ChatbotException
     * @deprecated use `Chatbot#command` API instead, removed in 2020-10
     */
    public boolean ismute(final String userId) throws ChatbotException {

        StringBuffer path = new StringBuffer();
        path.append("/users/");
        path.append(userId);
        path.append("/ismute");

        Response resp = command("POST", path.toString());

        if (resp.getRc() != 0) {
            throw new ChatbotException("Unable to check mute status, bad response from server.");
        }

        return ((JSONObject) resp.getData()).getBoolean("mute");
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
