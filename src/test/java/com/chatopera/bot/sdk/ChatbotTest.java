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
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.json.JSONObject;

import java.net.MalformedURLException;

public class ChatbotTest extends TestCase {

    private Chatbot cb;

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public ChatbotTest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(ChatbotTest.class);
    }

    public void setUp() {
        try {
            String clientId = "5be2bec4e267ce0011188199";
            String secret = "bf022fdb3b94efc8820e42dca27c8ec0";
            this.cb = new Chatbot(clientId, secret);
//            this.cb = new Chatbot("http://bot.chatopera.com");
//            this.cb = new Chatbot("http://superadmin.chatopera.com:8003/v1");
        } catch (ChatbotException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Rigourous Test :-)
     */
    public void testChatbot() {
        assertEquals(this.cb.getPort(), -1);
    }

    public void testGetChatbot() {
        try {
            JSONObject resp = this.cb.details();
            System.out.println("[testGetChatbot] " + resp.toString());
            assertEquals(0, resp.getInt("rc"));
        } catch (ChatbotException e) {
            e.printStackTrace();
        }
    }

    public void testExists() {
        try {
            assertTrue(this.cb.exists());
        } catch (ChatbotException e) {
            e.printStackTrace();
        }
    }

    public void testGetUsers() {
        try {
            JSONObject resp = this.cb.users(1, 20);
            System.out.println("[testGetUsers] resp " + resp.toString());
        } catch (ChatbotException e) {
            e.printStackTrace();
        }
    }


    public void testGetChats() {
        try {
            JSONObject resp = this.cb.chats("sdktest", 1, 20);
            System.out.println("[testGetChats] resp " + resp.toString());
        } catch (ChatbotException e) {
            e.printStackTrace();
        }
    }


//    public void testMuteUser() {
//        try {
//            this.cb.mute( "sdktest");
//            System.out.println("[testMuteUser] done. ");
//        } catch (ChatbotException e) {
//            e.printStackTrace();
//        }
//    }

    public void testUnMuteUser() {
        try {
            this.cb.unmute("sdktest");
            System.out.println("[testUnMuteUser] done. ");
        } catch (ChatbotException e) {
            e.printStackTrace();
        }
    }

    public void testIsMuteUser() {
        try {
            this.cb.ismute("sdktest");
            System.out.println("[testIsMuteUser] done. ");
        } catch (ChatbotException e) {
            e.printStackTrace();
        }
    }

    public void testConversation() {
        try {
            JSONObject resp = this.cb.conversation("sdktest", "华夏春松在哪里");
            System.out.println("[testConversation] resp " + resp.toString());
        } catch (ChatbotException e) {
            e.printStackTrace();
        }
    }

    public void testFaq() {
        try {
            JSONObject resp = this.cb.faq("sdktest", "华夏春松在哪里");
            System.out.print("[testFaq] resp " + resp.toString());
        } catch (ChatbotException e) {
            e.printStackTrace();
        }
    }

//    public void testParseUrl() {
//        try {
//            Chatbot c = new Chatbot("https://local:8000/");
//            System.out.println("chatbot baseUrl " + c.getBaseUrl());
//            assertEquals("https://local:8000/api/v1", c.getBaseUrl());
//        } catch (ChatbotException e) {
//            e.printStackTrace();
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//        }
//    }

//    public void testCreateBot() {
//        try {
//            JSONObject j = this.cb.createBot("cc_bot_2",
//                    "小云2",
//                    "zh_CN",
//                    "我不了解。",
//                    "小云机器人",
//                    "你好，我是小云。");
//        } catch (ChatbotException e) {
//            e.printStackTrace();
//        }
//    }


}
