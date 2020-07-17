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

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

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
            String clientId = Utils.getEnv("CLIENT_ID", "");
            String secret = Utils.getEnv("CLIENT_SECRET", "");
            String provider = Utils.getEnv("BOT_PROVIDER", "https://bot.chatopera.com");
            this.cb = new Chatbot(clientId, secret, provider);
        } catch (ChatbotException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public void testGetChatbot() throws ChatbotException {
        Response resp = this.cb.command("GET", "/");
        System.out.println("[testGetChatbot] " + resp.toJSON().toString());
        assertEquals(0, resp.getRc());
    }

    public void testExists() throws ChatbotException {
        assertTrue(this.cb.exists());
    }


    /***
     * 聊天机器人检索管理
     ***/

    public void testConversation() throws ChatbotException {
        JSONObject resp = this.cb.conversation("sdktest", "你好");
        System.out.println("[testConversation] resp " + resp.toString());
    }

    public void testFaq() throws ChatbotException {
        JSONObject resp = this.cb.faq("sdktest", "问题");
        System.out.print("[testFaq] resp " + resp.toString());
    }

    /***
     * 知识库标准问管理
     ***/

    public void testFaqcreate() throws ChatbotException {
        List<String> categories = new ArrayList<String>();
        JSONObject resp = this.cb.faqcreate("专业技能培训", "回答123", false, categories);
        System.out.print("[testFaq] testFaqcreate " + resp.toString());
    }

    public void testFaqlist() throws ChatbotException, UnsupportedEncodingException {
        JSONObject resp = this.cb.faqlist("专业", null, 1, 10);
        System.out.print("[testFaq] testFaqlist " + resp.toString());
    }

    public void testFaqdetail() throws ChatbotException {
        JSONObject resp = this.cb.faqdetail("AXNc87-rfXOJhfysI4ce");
        System.out.print("[testFaq] testFaqlist " + resp.toString());
    }

    public void testFaqupdate() throws ChatbotException {
        List<String> categories = new ArrayList<String>();
        JSONObject resp = this.cb.faqupdate("AXNc87-rfXOJhfysI4ce", "你专业吗", "我很专业", false, categories);
        System.out.print("[testFaq] testFaqupdate " + resp.toString());
        // 更新操作还支持禁用和启用操作
        JSONObject resp1 = this.cb.faqdisable("AXNc87-rfXOJhfysI4ce"); // 禁用
        System.out.print("[testFaq] testFaqdisable " + resp1.toString());
        JSONObject resp2 = this.cb.faqenable("AXNc87-rfXOJhfysI4ce");  // 启用
        System.out.print("[testFaq] testFaqdisable " + resp2.toString());
    }

    public void testFaqdelete() throws ChatbotException {
        JSONObject resp = this.cb.faqdelete("AXNc87-rfXOJhfysI4ce");
        System.out.print("[testFaq] testFaqdelete " + resp.toString());
    }


    /***
     * 知识库扩展问管理
     ***/

    public void testFaqextend() throws ChatbotException {
        List<String> categories = new ArrayList<String>();
        JSONObject resp = this.cb.faqcreate("专业技能培训", "回答123", false, categories);
        JSONObject resp2 = this.cb.faqextend(resp.getJSONObject("data").getString("id"));
        System.out.print("[testFaq] testFaqextend " + resp2.toString());
    }

    public void testFaqextendcreate() throws ChatbotException {
        JSONObject resp = this.cb.faqcreate("专业技能培训", "回答123", false, null);
        JSONObject resp2 = this.cb.faqextendcreate(resp.getJSONObject("data").getString("id"), "什么是专业化服务");
        System.out.print("[testFaq] faqextendcreate " + resp2.toString());
    }

    public void testFaqextendupdate() throws ChatbotException {
        JSONObject resp = this.cb.faqextendupdate("AWcq4ad_Cg-0XBpuH7tJ", "AXNdGtYgfXOJhfysI4co", "你怎么不够专业化");
        System.out.print("[testFaq] faqextendupdate " + resp.toString());
    }

    public void testFaqextenddelete() throws ChatbotException {
        JSONObject resp = this.cb.faqextenddelete("AWcq4ad_Cg-0XBpuH7tJ", "AWvMy0YREVcg4sphWFfG");
        System.out.print("[testFaq] faqextenddelete " + resp.toString());
    }

    /***
     * 知识库分类管理
     ***/

    public void testFaqcatetorycreate() throws ChatbotException {
        JSONObject resp = this.cb.faqcategorycreate("一级", null);
        System.out.print("[testFaq] faqcategorycreate " + resp.toString());
    }

    public void testFaqcatetories() throws ChatbotException {
        JSONObject resp = this.cb.faqcategories();
        System.out.print("[testFaq] faqcategories " + resp.toString());
    }

    public void testFaqcatetoryupdate() throws ChatbotException {
        JSONObject resp = this.cb.faqcategoryupdate("tXwSUy8Yy", "新一级");
        System.out.print("[testFaq] faqcategoryupdate " + resp.toString());
    }

    public void testFaqcatetorydelete() throws ChatbotException {
        JSONObject resp = this.cb.faqcategorydelete("tXwSUy8Yy");
        System.out.print("[testFaq] faqcategorydelete " + resp.toString());
    }


    /**
     * 创建意图识别会话
     */
    public void testIntentSession() throws ChatbotException {
        JSONObject resp = this.cb.intentsession("uid007", "javasdk");
        System.out.print("[testIntentSession] intent session " + resp.toString());
    }

    /**
     * 获取意图识别会话详情
     */
    public void testIntentSessionDetail() throws ChatbotException {
        JSONObject resp = this.cb.intentsession("D801D03243A65EA7A37736399078A209");
        System.out.print("[testIntentSessionDetail] intent session " + resp.toString());
    }

    /**
     * 和意图识别对话
     */
    public void testIntent() throws ChatbotException {
        JSONObject resp = this.cb.intent("D801D03243A65EA7A37736399078A209", "uid007", "我想查看现有车型");
        System.out.print("[testIntent] intent  " + resp.toString());
    }

    /**
     * 测试技能：心理问答 API 查询接口
     * ./admin/test.sh ChatbotTest#testPsychSearch
     */
    public void testPsychSearch() throws ChatbotException {
        JSONObject resp = this.cb.psychSearch("确定自己是否有抑郁倾向，想要知道自己当下该怎么办", 0.2);
        System.out.print("[testPsychSearch] psychSearch  " + resp.toString());
    }

    /**
     * 测试技能：心理问答 API 聊天接口
     * ./admin/test.sh ChatbotTest#testPsychChat
     */
    public void testPsychChat() throws ChatbotException {
        JSONObject resp = this.cb.psychChat("sdk", "appid001", "u001", "确定自己是否有抑郁倾向，想要知道自己当下该怎么办");
        System.out.print("[testPsychChat] psychChat  " + resp.toString());
    }

    /***
     * 聊天机器人用户管理
     ***/

    public void testGetUsers() throws ChatbotException {
        JSONObject resp = this.cb.users(1, 20);
        System.out.println("[testGetUsers] resp " + resp.toString());
    }
//
//    public void testGetChats() {
//        try {
//            JSONObject resp = this.cb.chats("sdktest", 1, 20);
//            System.out.println("[testGetChats] resp " + resp.toString());
//        } catch (ChatbotException e) {
//            e.printStackTrace();
//        }
//    }
//
////    public void testMuteUser() {
////        try {
////            this.cb.mute( "sdktest");
////            System.out.println("[testMuteUser] done. ");
////        } catch (ChatbotException e) {
////            e.printStackTrace();
////        }
////    }
//
//    public void testUnMuteUser() {
//        try {
//            this.cb.unmute("sdktest");
//            System.out.println("[testUnMuteUser] done. ");
//        } catch (ChatbotException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void testIsMuteUser() {
//        try {
//            this.cb.ismute("sdktest");
//            System.out.println("[testIsMuteUser] done. ");
//        } catch (ChatbotException e) {
//            e.printStackTrace();
//        }
//    }
//

}
