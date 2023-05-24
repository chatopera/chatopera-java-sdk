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
import com.chatopera.bot.sdk.models.Status;
import com.chatopera.bot.utils.EnvUtil;
import com.chatopera.bot.utils.Logger;
import junit.framework.TestCase;
import org.json.JSONObject;

import java.io.IOException;

public class TrainMgrTest extends TestCase {

    private TrainMgr trainMgr;
    private String botClientId;
    private String botClientSecret;


    public void setUp() throws IOException, ChatbotException {
        // 开启 SDK 日志，也可以通过设置环境变量 CHATOPERA_SDK_LOG_TRACE=on|off 来控制
        Logger.setEnabled(true);

        // 从系统环境变量中读取机器人密钥
        botClientId = EnvUtil.getEnv("BOT_CLIENT_ID", "");
        botClientSecret = EnvUtil.getEnv("BOT_CLIENT_SECRET", "");

        this.trainMgr = new TrainMgr(botClientId,
                botClientSecret,
                "https://bot.chatopera.com");
    }

    public void testGetStatus() throws ResourceInvalidException, ChatbotException {
        Status s = this.trainMgr.getStatus();
        System.out.println("getReindex " + Integer.toString(s.getReindex()));
        System.out.println("getReparse " + Integer.toString(s.getReparse()));
        System.out.println("getRetrain " + Integer.toString(s.getRetrain()));
        Logger.trace(String.format("status %s", s.toString()));
    }


    /**
     * 测试训练多轮对话
     */
    public void testTrainConversations() throws ChatbotException, ResourceInvalidException {
        boolean result = this.trainMgr.trainConversations();
        assertTrue(result); // 结果为 true, 代表服务器完成同步任务
    }

    /**
     * 测试训练意图识别
     *
     * @throws ChatbotException
     * @throws ResourceInvalidException
     * @throws ResourceOperationException
     */
    public void testTrainIntents() throws ChatbotException, ResourceInvalidException, ResourceOperationException {
        boolean result = this.trainMgr.trainIntents();
        assertTrue(result); // 结果为 true, 代表服务器完成同步任务
    }


    /**
     * 测试训练知识库
     *
     * @throws ResourceInvalidException
     * @throws ChatbotException
     */
    public void testTrainFAQs() throws ResourceInvalidException, ChatbotException {
        boolean result = this.trainMgr.trainFAQs();
        assertTrue(result); // 结果为 true, 代表服务器完成同步任务
    }

    /**
     * 测试训练和等待结果
     *
     * @throws ResourceInvalidException
     * @throws ChatbotException
     * @throws InterruptedException
     */
    public void testTrainAllAndWait() throws ResourceInvalidException, ChatbotException, InterruptedException {
        // 提交训练任务
        this.trainMgr.trainAll();
        // 等待训练结束
        this.trainMgr.waitForJobsDone();
    }
}
