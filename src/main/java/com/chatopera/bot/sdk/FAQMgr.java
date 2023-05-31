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

import com.chatopera.bot.exception.ChatbotException;
import com.chatopera.bot.sdk.basics.Response;
import org.apache.commons.lang3.StringUtils;

import java.net.MalformedURLException;

/**
 * 知识库问答管理
 */
public class FAQMgr {

    private Chatbot chatbot;

    // 不支持定义空实例
    private FAQMgr() {
    }

    public FAQMgr(final String clientId, final String clientSecret, final String baseUrl) throws MalformedURLException, ChatbotException {
        this.chatbot = new Chatbot(clientId, clientSecret, baseUrl);
    }

    public FAQMgr(final String clientId, final String clientSecret) throws MalformedURLException, ChatbotException {
        this.chatbot = new Chatbot(clientId, clientSecret);
    }

    /**
     * Get QnA Pairs by page
     * 支持分页：page 第几页，从 1 开始；limit 每页数据条数
     *
     * @param page  Page
     * @param limit Page size, records in page
     * @return
     */
    public Response getFaqs(int page, int limit) throws ChatbotException {
        if (page <= 0) {
            page = 1;
        }

        if (limit <= 0) {
            limit = 20;
        }

        Response resp = this.chatbot.command("GET", String.format("/faq/database?q=&&page=%d&limit=%d", page, limit));

        if (resp == null || resp.getRc() != 0) {
            throw new ChatbotException(String.format("Invalid response data[%s]", StringUtils.isNotBlank(resp.getError()) ? resp.getError() : ""));
        }

        return resp;
    }
}
