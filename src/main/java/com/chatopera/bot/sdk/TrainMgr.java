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
import com.chatopera.bot.exception.ResourceInvalidException;
import com.chatopera.bot.exception.ResourceOperationException;
import com.chatopera.bot.sdk.basics.Response;
import com.chatopera.bot.sdk.models.Status;
import com.chatopera.bot.utils.Logger;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.net.MalformedURLException;

/**
 * 训练管理
 */
public class TrainMgr {

    private Chatbot chatbot;

    // 不支持定义空实例
    private TrainMgr() {
    }

    public TrainMgr(final String clientId, final String clientSecret, final String baseUrl) throws MalformedURLException, ChatbotException, MalformedURLException, ChatbotException {
        this.chatbot = new Chatbot(clientId, clientSecret, baseUrl);
    }

    public TrainMgr(final String clientId, final String clientSecret) throws MalformedURLException, ChatbotException {
        this.chatbot = new Chatbot(clientId, clientSecret);
    }

    /**
     * Get Chatbot Status
     *
     * @return
     */
    public Status getStatus() throws ChatbotException, ResourceInvalidException {
        Response resp = this.chatbot.command("GET", "/clause/devver/build");
        if (resp.getRc() == 0) {
            Status status = new Status(resp.getStatus());
            return status;
        } else {
            throw new ChatbotException("Unexpected status result. " + (StringUtils.isNotBlank(resp.getError()) ? resp.getError() : ""));
        }
    }

    /**
     * 训练多轮对话脚本
     *
     * @return
     */
    protected boolean trainConversations() throws ChatbotException, ResourceInvalidException {
        Status currentStatus = getStatus();

        if (currentStatus.getReparse() != 0) {
            // 多轮对话待同步
            Response resp = this.chatbot.command("POST", "/conversation/sync/customdicts");
            return resp.getRc() == 0;
        } else {
            // 服务器端机器人多轮对话已经同步最新词典
            return true;
        }
    }

    /**
     * 训练意图识别，返回提交结果，该任务提交成功后，异步执行。
     *
     * @return true: 开始执行训练（此时服务器在执行任务，训练时间取决于数据量）；false：不能正常执行训练
     * @throws ChatbotException
     * @throws ResourceInvalidException
     */
    protected boolean trainIntents() throws ChatbotException, ResourceInvalidException, ResourceOperationException {
        Status currentStatus = getStatus();

        if (currentStatus.getRetrain() != 0) {
            // 意图识别模型待训练
            Response resp = this.chatbot.command("POST", "/clause/devver/train");
            if (resp.getRc() == 0) {
                // 提交并开始执行训练
                return true;
            } else if (resp.getRc() == 21 || resp.getRc() == 22 || resp.getRc() == 24) {
                Logger.warn("[trainIntents] 没有意图或意图没有说法，此时不需要训练。");
                return true;
            } else if (resp.getRc() == 25) {
                Logger.warn("[trainIntents] 存在不合法的词典信息，无法开始训练");
                return false;
            } else {
                throw new ResourceOperationException("[trainIntents] Unexpected operation results.");
            }
        } else {
            // 训练任务未开始，因为服务器端意图识别模型已经同步最新的训练数据
            // 不需要重新训练
            return true;
        }
    }


    /**
     * 训练知识库，该任务提交成功后，异步执行。
     *
     * @return 训练任务是否启动。
     */
    protected boolean trainFAQs() throws ResourceInvalidException, ChatbotException {
        Status currentStatus = getStatus();

        if (currentStatus.getReindex() != 0) {
            // 多轮对话待同步
            Response resp = this.chatbot.command("POST", "/faq/sync/customdicts");
            return resp.getRc() == 0;
        } else {
            // 服务器端机器人知识库索引已经与最新词典信息一致，不需要重新训练
            return true;
        }
    }

    /**
     * Start in parallel, train all domains
     */
    public void trainAll() {

        try {
            this.trainConversations();
        } catch (Exception e) {
            Logger.trace("[trainAll] unexpected result to start train conversation.");
            e.printStackTrace();
        }

        try {
            this.trainFAQs();
        } catch (Exception e) {
            Logger.trace("[trainAll] unexpected result to start train faqs.");
            e.printStackTrace();
        }

        try {
            this.trainIntents();
        } catch (Exception e) {
            Logger.trace("[trainAll] unexpected result to start train intents.");
            e.printStackTrace();
        }
    }

    /**
     * 等待训练任务结束
     */
    public void waitForJobsDone() throws InterruptedException, ResourceInvalidException, ChatbotException {
        while (!isUpdated()) {
            Logger.warn("TrainMgr [waitForJobsDone] still in progress ...");
            Thread.sleep(10000);
        }
    }


    /**
     * 检查机器人的训练任务是否已经结束
     *
     * @return
     */
    private boolean isUpdated() throws ResourceInvalidException, ChatbotException {
        final Status status = getStatus();

        boolean isUpdatedIntents = true;
        boolean isUpdatedFAQs = true;

        /**
         * 检查多轮对话状态
         *  多轮对话的重新训练不是异步的，在提交了训练任务后，是立即返回结果的
         */
        boolean isUpdatedConversations = true;

        /**
         * 检查意图识别状态
         */
        if (status.getRetrain() == 1) {
            // 同步中
            isUpdatedIntents = false;
        } else {
            // 以下情况，返回 true
            // * 意图识别完成同步
            // * 训练过程中，意图识别数据发生变化，本次训练不有意义
            isUpdatedIntents = true;
        }

        /**
         * 检查 FAQs 状态
         */
        if (status.getReindex() == 1) {
            // 正在更新
            isUpdatedFAQs = false;
        } else {
            // 以下情况，返回 true
            // * 已经同步
            // * 知识库或自定义词典变更，无法继续同步，可返回后重新提交。
            isUpdatedFAQs = true;
        }


        return isUpdatedIntents && isUpdatedFAQs && isUpdatedConversations;
    }


}
