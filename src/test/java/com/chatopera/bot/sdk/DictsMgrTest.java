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

import com.chatopera.bot.exception.*;
import com.chatopera.bot.sdk.basics.Response;
import com.chatopera.bot.sdk.models.DictWord;
import com.chatopera.bot.utils.EnvUtil;
import com.chatopera.bot.utils.Logger;
import junit.framework.TestCase;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

public class DictsMgrTest extends TestCase {

    private DictsMgr dictsMgr;
    private String botClientId;
    private String botClientSecret;

    public void setUp() throws IOException, ChatbotException {
        // 开启 SDK 日志，也可以通过设置环境变量 CHATOPERA_SDK_LOG_TRACE=on|off 来控制
        Logger.setEnabled(true);

        // 从系统环境变量中读取机器人密钥
        botClientId = EnvUtil.getEnv("BOT_CLIENT_ID", "");
        botClientSecret = EnvUtil.getEnv("BOT_CLIENT_SECRET", "");

        this.dictsMgr = new DictsMgr(botClientId,
                botClientSecret,
                "https://bot.chatopera.com");
    }

    /**
     * 自定义词典：获取自定义词典列表
     *
     * @throws ChatbotException
     */
    public void testGetCustomDicts() throws ChatbotException {
        Response resp = this.dictsMgr.getCustomDicts(1, 3);
        Logger.trace("[getCustomDicts] resp total customdict records " // 所有自定义词典数量，包括词条词典和正则表达式词典
                + resp.getTotal()
                + ", total pages " + resp.getTotal_page()
                + ", current page index " + resp.getCurrent_page()
                + ", current page data records "
                + (((JSONArray) resp.getData()).length()));

        JSONArray data = ((JSONArray) resp.getData()); // 自定义词典的数组

        // 打印当前页词典信息
        for (int i = 0; i < data.length(); i++) {
            JSONObject dict = (JSONObject) data.get(i);
            // JSONObject 字段：name, 词典名称；type, 词典类型（regex 正则表达式词典，vocab 词汇表词典）；
            Logger.trace("  dictname: " + dict.getString("name")
                    + ", type " + dict.getString("type"));
        }
    }

    /**
     * 删除自定义词典
     *
     * @throws ChatbotException
     */
    public void testDeleteCustomDict() throws ChatbotException {
        // 词典标识名
        String dictname = "fruit";
        boolean done = this.dictsMgr.deleteCustomDict(dictname);
        // done: true, 删除成功；false，删除失败
        assertTrue(done);
    }

    /**
     * 自定义词典：获得自定义词典基本信息
     *
     * @throws ChatbotException
     * @throws ResourceNotExistException
     */
    public void testGetCustomDict() throws ChatbotException, ResourceNotExistException {
        String dictname = "fruit";
        JSONObject dict = this.dictsMgr.getCustomDict(dictname);
        assertEquals(dict.getString("name"), dictname);
        Logger.trace("Dict info" + dict.toString());
        // dict: name 词典标识，createdate 创建时间，type 类型，updatedate 最后更新时间

        dictname = "fruit2";
        boolean catched = false;
        try {
            JSONObject dict2 = this.dictsMgr.getCustomDict(dictname);
        } catch (ResourceNotExistException e) {
            Logger.trace("Dict not exist");
            catched = true;
        }

        assertTrue(catched);
    }

    /**
     * 自定义词典：测试创建自定义词汇表词典
     */
    public void testCreateCustomVocabDict() throws ChatbotException, ResourceNotCreatedException, ResourceExistedException {
        String dictname = "fruit";
        String description = "水果";
        JSONObject dict = this.dictsMgr.createCustomVocabDict(dictname, description);
        Logger.trace("[testCreateCustomVocabDict] dict " + dict.toString());
        // sample json {"updatedate":"2023-05-13 11:03:58","vendor":null,"name":"fruit4","description":null,"createdate":"2023-05-13 11:03:58","used":null,"type":"vocab","samples":null}
        assertEquals(dict.getString("name"), dictname);
        assertEquals(dict.getString("type"), "vocab");
    }

    /**
     * 自定义词典：创建或更新自定义词条词典的词条
     */
    public void testPutCustomVocabDictWord() throws ResourceInvalidException, ChatbotException {
        String dictname = "fruit";

        // 词条
        DictWord dictword = new DictWord();
        dictword.setWord("马铃薯");
        // 可以添加多个同义词
        dictword.addSynonym("土豆");
        dictword.addSynonym("洋芋");
        dictword.addSynonym("山药蛋");

        boolean result = this.dictsMgr.putCustomVocabDictWord(dictname, dictword);
        assertTrue(result);
    }

    /**
     * 自定义词典：获得词典词条详情
     *
     * @throws ChatbotException
     * @throws ResourceNotCreatedException
     * @throws ResourceExistedException
     */
    public void testGetCustomVocabDictWord() throws ChatbotException, ResourceNotCreatedException, ResourceExistedException, ResourceNotExistException, ResourceInvalidException {
        String dictname = "fruit";
        String dictword = "马铃薯";
        DictWord result = this.dictsMgr.getCustomVocabDictWord(dictname, dictword);
        Logger.trace("[testGetCustomVocabDictWord] dict " + result.toJson().toString());
        assertEquals(result.getWord(), dictword);
        Logger.trace("Synonyms " + result.stringifySynonyms());
        // Sample output
        //        2023-05-15 07:04:13 [testGetCustomVocabDictWord] dict {"synonyms":"山药蛋;土豆;洋芋","word":"马铃薯"}
        //        2023-05-15 07:04:13 Synonyms 山药蛋;土豆;洋芋
    }


    /**
     * 自定义词典：获得词条列表
     *
     * @throws ChatbotException
     * @throws ResourceNotCreatedException
     * @throws ResourceExistedException
     * @throws ResourceNotExistException
     * @throws ResourceInvalidException
     */
    public void testGetCustomVocabDictWords() throws ChatbotException, ResourceNotCreatedException, ResourceExistedException, ResourceNotExistException, ResourceInvalidException {
        String dictname = "fruit";
        int page = 1;
        int limit = 10;
        Response result = this.dictsMgr.getCustomVocabDictWords(dictname, page, limit);
        Logger.trace(String.format("[testGetCustomVocabDictWords] 该词典词条总数 %d", result.getTotal()));
        Logger.trace(String.format("[testGetCustomVocabDictWords] 总页数 %d", result.getTotal_page()));
        Logger.trace(String.format("[testGetCustomVocabDictWords] 当前页 %d", result.getCurrent_page()));
        Logger.trace(String.format("[testGetCustomVocabDictWords] 当前页数据条数 %d", ((JSONArray) result.getData()).length()));

        // 遍历词条
        JSONArray data = (JSONArray) result.getData();
        for (int i = 0; i < data.length(); i++) {
            DictWord d = new DictWord();
            d.fromJson(data.getJSONObject(i));
            Logger.trace(String.format("[testGetCustomVocabDictWords] word %s, syns %s", d.getWord(), d.stringifySynonyms()));
        }
        // Sample output
        //        2023-05-15 07:02:31 [testGetCustomVocabDictWords] 该词典词条总数 5
        //        2023-05-15 07:02:31 [testGetCustomVocabDictWords] 总页数 1
        //        2023-05-15 07:02:31 [testGetCustomVocabDictWords] 当前页 1
        //        2023-05-15 07:02:31 [testGetCustomVocabDictWords] 当前页数据条数 5
        //        2023-05-15 07:02:31 [testGetCustomVocabDictWords] word 荔枝, syns
        //        2023-05-15 07:02:31 [testGetCustomVocabDictWords] word 西瓜, syns
        //        2023-05-15 07:02:31 [testGetCustomVocabDictWords] word 香蕉, syns
        //        2023-05-15 07:02:31 [testGetCustomVocabDictWords] word 猕猴桃, syns
        //        2023-05-15 07:02:31 [testGetCustomVocabDictWords] word 马铃薯, syns 山药蛋;土豆;洋芋
    }


    /**
     * 自定义词典：删除自定义词条 通过指定词典和标准词
     */
    public void testDeleteCustomVocabDictWord() throws ResourceOperationException, ChatbotException {
        String dictname = "fruit";
        String dictword = "马铃薯"; // 标准词

        boolean result = this.dictsMgr.deleteCustomVocabDictWord(dictname, dictword);
        assertTrue(result);
    }

}
