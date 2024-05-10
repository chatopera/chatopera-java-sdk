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
import com.chatopera.bot.exception.ChatbotException;
import com.chatopera.bot.utils.EnvUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.json.JSONObject;

import java.net.MalformedURLException;

public class ChatoperaTest extends TestCase {

    private Chatopera co;


    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public ChatoperaTest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(ChatoperaTest.class);
    }

    public void setUp() {
        try {
            String accessToken = EnvUtil.getEnv("BOT_ACCESS_TOKEN", "");
            String provider = EnvUtil.getEnv("BOT_PROVIDER", "https://bot.chatopera.com");
            System.out.println("provider: " + provider + ", accessToken: " + accessToken);
            this.co = new Chatopera(accessToken, provider);
        } catch (ChatbotException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建机器人
     *
     * @throws ChatbotException
     */
    public void testCreateChatbot() throws ChatbotException {
        JSONObject payload = new JSONObject();
        payload.put("name", "TestJavaSDK" + Long.toString(System.currentTimeMillis()));
        payload.put("description", "Test with Java SDK");
        payload.put("primaryLanguage", "zh_CN");
        payload.put("trans_zhCN_ZhTw2ZhCn", false);
        Response resp = this.co.command("POST", "/chatbot", payload);
        System.out.println("[testCreateChatbot] " + resp.toJSON().toString());
        assertEquals(0, resp.getRc());
    }

    /**
     * 获取机器人列表
     *
     * @throws ChatbotException
     */
    public void testGetChatbots() throws ChatbotException {
        Response resp = this.co.command("GET", "/chatbot");
        System.out.println("[testGetChatbots] " + resp.toJSON().toString());
        assertEquals(0, resp.getRc());
    }

    /**
     * 通过聊天机器人 ID 获得 Secret 信息
     *
     * @throws ChatbotException
     */
    public void testGetChatbotSecretById() throws ChatbotException {
        String chatbotID = "YOUR CLIENT ID";
        Response resp = this.co.command("GET", "/secret/" + chatbotID);
        System.out.println("[testGetChatbotSecretById] " + resp.toJSON().toString());
//        {
//  "rc": 0,
//  "data": {
//    "createdAt": "Fri May 10 2024 17:11:52 GMT+0800 (China Standard Time)",
//    "isDeleted": false,
//    "name": "testdel3",
//    "logo": "",
//    "secret": "2ba7f080adbb80f67ec5dc9d3c731b7d",
//    "descriptions": ""
//  }
//}
//
        assertEquals(0, resp.getRc());
    }


    /**
     * 获得聊天机器人详情
     *
     * @throws ChatbotException
     */
    public void testGetChatbotDetails() throws ChatbotException {
        String chatbotID = "YOUR CLIENT ID";
        Response resp = this.co.command("GET", "/chatbot/" + chatbotID);
        System.out.println("[testGetChatbotDetails] " + resp.toJSON().toString());
        assertEquals(0, resp.getRc());
        //{
        //  "rc": 0,
        //  "data": {
        //    "faqSuggReplyThreshold": 0.5,
        //    "description": "",
        //    "conversationTimeout": 1800,
        //    "gambitIntentQuestionMaxAttempts": 2,
        //    "name": "春松客服演示机器人",
        //    "gambitLikeThreshold": 0.8,
        //    "faqBestReplyThreshold": 0.8,
        //    "welcome": "你好！我是春松客服演示机器人客服。",
        //    "fallback": "我不明白您的意思。发送【转人工】，进入人工客服。",
        //    "trans_zhCN_ZhTw2ZhCn": false,
        //    "primaryLanguage": "zh_CN",
        //    "historyCheckpoints": 100,
        //    "llmBaiduErnieBot": false,
        //    "status": {
        //      "retrain": 0,
        //      "reindex": 0,
        //      "reparse": 0
        //    }
        //  }
        //}
    }

    /**
     * 删除聊天机器人
     *
     * @throws ChatbotException
     */
    public void testDeleteChatbots() throws ChatbotException, MalformedURLException {
        String chatbotID = "YOUR CLIENT ID";

        // 首先，使用 BOT 的 ClientId 获得 secret
        Response resp = this.co.command("GET", "/secret/" + chatbotID);
        System.out.println("[testDeleteChatbots] secret " + resp.toJSON().toString());
//        {
//            "rc": 0,
//                "data": {
//            "createdAt": "Fri May 10 2024 17:11:52 GMT+0800 (China Standard Time)",
//                    "isDeleted": false,
//                    "name": "testdel3",
//                    "logo": "",
//                    "secret": "2ba7f080adbb80f67",
//                    "descriptions": ""
//        }
//        }

        if (resp.getRc() == 0) {
            // 然后，通过 Chatbot 类创建 chatbot，再调用 chatbot 的删除方法
            JSONObject botInfo = (JSONObject) resp.getData();
            Chatbot cb = new Chatbot(chatbotID, botInfo.getString("secret"));
            Response resp2 = cb.command("DELETE", "/");
            assertEquals(0, resp2.getRc());
            System.out.println("[testDeleteChatbots] " + resp2.toJSON().toString());
        }
    }
}
