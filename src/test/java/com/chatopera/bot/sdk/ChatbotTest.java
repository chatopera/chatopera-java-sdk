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

            // Svt System
//            String clientId = "5d72168381df8400178e3952";
//            String secret = "f0fc91ac86e10b09cdeec15793c9f18c";
//            String provider = "http://165.192.71.23:6558";

            // Dev System
//            String clientId = "5d6e5e71f401950017599143";
//            String secret = "37d7a4c52b4311dc9694dd9a5628d8a8";
//            String provider = "http://165.192.71.23:5558";

            // Production
            String clientId = "5e847b30bd822e0017ed9bce";
            String secret = "b9ec7a3af7cf41e62f3c850009fe61dc";
//            String provider = "http://123.56.191.5:5558";
            String provider = "https://bot.chatopera.com";

            this.cb = new Chatbot(clientId, secret, provider);
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

    /***
     * 聊天机器人用户管理
     ***/

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

    /***
     * 聊天机器人检索管理
     ***/

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


    /***
     * 知识库标准问管理
     ***/

    public void testFaqcreate() {
        try {
            List<String> categories = new ArrayList<String>();
            JSONObject resp = this.cb.faqcreate("专业技能培训", "回答123", false, categories);
            System.out.print("[testFaq] testFaqcreate " + resp.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void testFaqlist() {
        try {
            JSONObject resp = this.cb.faqlist("专业",null ,1, 10);
            System.out.print("[testFaq] testFaqlist " + resp.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void testFaqdetail() {
        try {
            JSONObject resp = this.cb.faqdetail("AWcq4ad_Cg-0XBpuH7tJ");
            System.out.print("[testFaq] testFaqlist " + resp.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void testFaqupdate() {
        try {
            List<String> categories = new ArrayList<String>();
            JSONObject resp = this.cb.faqupdate("AWcq4ad_Cg-0XBpuH7tJ", "你专业吗", null, false, categories);
            System.out.print("[testFaq] testFaqupdate " + resp.toString());
            // 更新操作还支持禁用和启用操作
            this.cb.faqdisable("AWcq4ad_Cg-0XBpuH7tJ"); // 禁用
            this.cb.faqenable("AWcq4ad_Cg-0XBpuH7tJ");  // 启用
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void testFaqdelete() {
        try {
            JSONObject resp = this.cb.faqdelete("AWcq4aTiCg-0XBpuH7tE");
            System.out.print("[testFaq] testFaqdelete " + resp.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /***
     * 知识库扩展问管理
     ***/

    public void testFaqextend() {
        try {
            JSONObject resp = this.cb.faqextend("AWcq4ZJ7Cg-0XBpuH7sx");
            System.out.print("[testFaq] testFaqextend " + resp.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void testFaqextendcreate() {
        try {
            JSONObject resp = this.cb.faqextendcreate("AWcq4ad_Cg-0XBpuH7tJ", "什么是专业化服务");
            System.out.print("[testFaq] faqextendcreate " + resp.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void testFaqextendupdate() {
        try {
            JSONObject resp = this.cb.faqextendupdate("AWcq4ad_Cg-0XBpuH7tJ", "AWvMy0YREVcg4sphWFfG", "你怎么不够专业化");
            System.out.print("[testFaq] faqextendupdate " + resp.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void testFaqextenddelete() {
        try {
            JSONObject resp = this.cb.faqextenddelete("AWcq4ad_Cg-0XBpuH7tJ", "AWvMy0YREVcg4sphWFfG");
            System.out.print("[testFaq] faqextenddelete " + resp.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /***
     * 知识库分类管理
     ***/

    public void testFaqcatetorycreate() {
        try {
            JSONObject resp = this.cb.faqcategorycreate("一级", null);
            System.out.print("[testFaq] faqcategorycreate " + resp.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void testFaqcatetories() {
        try {
            JSONObject resp = this.cb.faqcategories();
            System.out.print("[testFaq] faqcategories " + resp.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void testFaqcatetoryupdate() {
        try {
            JSONObject resp = this.cb.faqcategoryupdate("tXwSUy8Yy", "新一级");
            System.out.print("[testFaq] faqcategoryupdate " + resp.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void testFaqcatetorydelete() {
        try {
            JSONObject resp = this.cb.faqcategorydelete("tXwSUy8Yy");
            System.out.print("[testFaq] faqcategorydelete " + resp.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 创建意图识别会话
     */
    public void testIntentSession(){
        try {
            JSONObject resp = this.cb.intentsession("uid007", "javasdk");
            System.out.print("[testIntentSession] intent session " + resp.toString());
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 获取意图识别会话详情
     */
    public void testIntentSessionDetail(){
        try {
            JSONObject resp = this.cb.intentsession("F340C30ADE96C0B9F4FD7B3500000000");
            System.out.print("[testIntentSessionDetail] intent session " + resp.toString());
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 和意图识别对话
     */
    public void testIntent(){
        try {
            JSONObject resp = this.cb.intent("CE6A44B3718BF5DA1C4AFA8B00000000", "uid007","送到远洋国际");
            System.out.print("[testIntent] intent  " + resp.toString());
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 测试技能：心理问答 API 查询接口
     * ./admin/test.sh ChatbotTest#testPsychSearch
     */
    public void testPsychSearch(){
        try {
            JSONObject resp = this.cb.psychSearch("确定自己是否有抑郁倾向，想要知道自己当下该怎么办", 0.2);
            System.out.print("[testPsychSearch] psychSearch  " + resp.toString());
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 测试技能：心理问答 API 聊天接口
     * ./admin/test.sh ChatbotTest#testPsychChat
     */
    public void testPsychChat(){
        try {
            JSONObject resp = this.cb.psychChat("sdk", "appid001", "u001", "确定自己是否有抑郁倾向，想要知道自己当下该怎么办");
            System.out.print("[testPsychChat] psychChat  " + resp.toString());
        }catch(Exception e){
            e.printStackTrace();
        }
    }


    /***
     * TODO 知识库近义词管理
     ***/


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
