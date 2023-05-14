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

import com.chatopera.bot.sdk.basics.Response;
import com.chatopera.bot.exception.*;
import com.chatopera.bot.sdk.models.DictWord;
import com.chatopera.bot.utils.Logger;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;

/**
 * 词典管理
 */
public class DictsMgr {

    private Chatbot chatbot;

    // 不支持定义空实例
    private DictsMgr() {
    }

    public DictsMgr(final String clientId, final String clientSecret, final String baseUrl) throws MalformedURLException, ChatbotException {
        this.chatbot = new Chatbot(clientId, clientSecret, baseUrl);
    }

    public DictsMgr(final String clientId, final String clientSecret) throws MalformedURLException, ChatbotException {
        this.chatbot = new Chatbot(clientId, clientSecret);
    }

    /**
     * Get Custom Vocab Dicts by page
     * 支持分页：page 第几页，从 1 开始；limit 每页数据条数
     *
     * @param page  Page
     * @param limit Page size, records in page
     * @return
     */
    public Response getCustomDicts(int page, int limit) throws ChatbotException {
        if (page <= 0) {
            page = 1;
        }

        if (limit <= 0) {
            limit = 20;
        }

        Response resp = this.chatbot.command("GET", String.format("/clause/customdicts?page=%d&limit=%d", page, limit));

        if (resp == null || resp.getRc() != 0) {
            throw new ChatbotException(String.format("Invalid response data[%s]", StringUtils.isNotBlank(resp.getError()) ? resp.getError() : ""));
        }

        return resp;
    }

    public Chatbot getChatbot() {
        return this.chatbot;
    }

    /**
     * Remove custom dict by name
     *
     * @param dictname 词典标识名
     * @return true, 删除成功；false，删除失败
     * @throws ChatbotException
     */
    public boolean deleteCustomDict(final String dictname) throws ChatbotException {

        Response resp = this.chatbot.command("DELETE", String.format("/clause/customdicts/%s", dictname));

        if (resp.getRc() == 11) {
            // Not exist before remove, claim as true
            return true;
        } else if (resp.getRc() != 0) {
            Logger.warn(String.format("[deleteCustomDict] Invalid response data[%s], dictname %s", StringUtils.isNotBlank(resp.getError()) ? resp.getError() : "", dictname));
            return false;
        }

        return true;
    }

    /**
     * 获得自定义词典的基本信息
     *
     * @param dictname
     * @return
     * @throws ChatbotException
     */
    public JSONObject getCustomDict(final String dictname) throws ChatbotException, ResourceNotExistException {
        Response resp = this.chatbot.command("GET", String.format("/clause/customdict?name=%s", dictname));

        if (resp.getRc() == 0) {
            return (JSONObject) resp.getData();
        } else if (resp.getRc() == 2) {
            throw new ResourceNotExistException(String.format("Dict %s not exist.", dictname));
        } else {
            throw new ChatbotException(String.format("[getCustomDict] Invalid response data[%s], dictname %s", StringUtils.isNotBlank(resp.getError()) ? resp.getError() : "", dictname));
        }
    }


    /**
     * 创建自定义词汇表词典
     *
     * @param dictname 词典标识
     * @return
     * @throws ResourceExistedException
     * @throws ResourceNotCreatedException
     * @throws ChatbotException
     */
    public JSONObject createCustomVocabDict(final String dictname) throws ResourceExistedException, ResourceNotCreatedException, ChatbotException {
        return createCustomVocabDict(dictname, null);
    }

    /**
     * 创建自定义词汇表词典
     *
     * @param dictname
     * @return
     * @throws ChatbotException
     * @throws ResourceNotCreatedException
     * @throws ResourceExistedException
     */
    public JSONObject createCustomVocabDict(final String dictname, final String description) throws ChatbotException, ResourceNotCreatedException, ResourceExistedException {
        JSONObject payload = new JSONObject();
        payload.put("name", dictname);
        payload.put("type", "vocab");

        Response resp = this.chatbot.command("POST", "/clause/customdicts", payload);

        if (resp.getRc() == 0 && StringUtils.isNotBlank(description)) {
            // 更新词典描述
            JSONObject payload2 = new JSONObject();
            payload2.put("description", description);
            Response resp2 = this.chatbot.command("PUT", String.format("/clause/customdicts/%s", dictname), payload2);
            if (resp2.getRc() == 0) {
                JSONObject result = (JSONObject) resp2.getData();
                result.put("description", description);
                return result;
            }
        } else if (resp.getRc() == 2) {
            // Vocab existed
            throw new ResourceExistedException("Error, dict existed " + dictname);
        } else if (resp.getRc() != 0) {
            throw new ResourceNotCreatedException("Error, can not create " + dictname + ", " + (StringUtils.isNotBlank(resp.getError()) ? resp.getError() : "Unknown error."));
        }

        return (JSONObject) resp.getData();
    }

    /**
     * 创建或更新自定义词条词典的词条
     * 如果存在该标准词的词条，将会使用传入的 synonyms 作为近义词进行覆盖
     *
     * @param dictName
     * @param dictWord
     * @return
     */
    public boolean putCustomVocabDictWord(final String dictname, final DictWord dictword) throws ChatbotException {

        JSONObject payload = new JSONObject();

        // add customdict
        JSONObject customdict = new JSONObject();
        customdict.put("name", dictname);
        payload.put("customdict", customdict);

        // add dictword
        JSONObject dictwordJson = new JSONObject();
        dictwordJson.put("word", dictword.getWord());
        String synonyms = dictword.stringifySynonyms();
        if (StringUtils.isNotBlank(synonyms)) {
            dictwordJson.put("synonyms", synonyms);
        }

        payload.put("dictword", dictwordJson);

        Response resp = this.chatbot.command("POST", "/clause/dictwords", payload);

        if (resp.getRc() != 0) {
            if (StringUtils.isNotBlank(resp.getError())) {
                Logger.warn(resp.getError());
            }
            Logger.warn("[putCustomVocabDictWord] create dict failed " + (StringUtils.isNotBlank(resp.getError()) ? resp.getError() : ""));
        }

        return resp.getRc() == 0;
    }

    /**
     * 返回自定义词典词条，指定词典标识和标准词
     *
     * @param dictname
     * @param dictword
     * @return
     */
    public DictWord getCustomVocabDictWord(final String dictname, final String dictword) throws ChatbotException, ResourceNotExistException, ResourceInvalidException {
        String dictwordEncoded = null;
        try {
            dictwordEncoded = URLEncoder.encode(dictword, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            dictwordEncoded = URLEncoder.encode(dictword);
        }
        Response resp = this.chatbot.command("GET", String.format("/clause/dictword?dictword=%s&dictname=%s", dictwordEncoded, dictname));

        if (resp.getRc() == 0) {
            JSONObject data = (JSONObject) resp.getData();
            DictWord w = new DictWord();
            w.fromJson(data);
            return w;
        } else {
            throw new ResourceNotExistException("Error " + (StringUtils.isNotBlank(resp.getError()) ? resp.getError() : "RC " + resp.getRc()));
        }
    }


    /**
     * 删除自定义词典词条：指定标准词
     *
     * @param dictname 词典标识
     * @param dictword 标准词
     * @return
     */
    public boolean deleteCustomVocabDictWord(final String dictname, final String dictword) throws ChatbotException, ResourceOperationException {
        JSONObject payload = new JSONObject();
        payload.put("customdict", dictname);
        payload.put("dictword", dictword);

        Response resp = this.chatbot.command("DELETE", "/clause/dictwords", payload);

        if (resp.getRc() != 0) {
            Logger.trace("deleteCustomVocabDictWord -");
            Logger.trace(resp.getError());
            throw new ResourceOperationException("Invalid delete operation on Resource dict[" + dictname + "], dictword [" + dictword + "]");
        }

        return true;
    }

    /**
     * Get Custom Vocab Dictwords by page
     * 支持分页：page 第几页，从 1 开始；limit 每页数据条数
     *
     * @param dictname Custom vocab dict name
     * @param page     Page
     * @param limit    Page size, records in page
     * @return
     */
    public Response getCustomVocabDictWords(final String dictname, int page, int limit) throws ChatbotException {
        if (page <= 0) {
            page = 1;
        }

        if (limit <= 0) {
            limit = 20;
        }

        Response resp = this.chatbot.command("GET", String.format("/clause/dictwords?customdict=%s&page=%d&limit=%d", dictname, page, limit));

        if (resp == null || resp.getRc() != 0) {
            throw new ChatbotException(String.format("Invalid response data[%s]", StringUtils.isNotBlank(resp.getError()) ? resp.getError() : ""));
        }

        return resp;
    }
}
