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
}
