package com.chatopera.bot.sdk;

import com.chatopera.bot.basics.Response;
import com.chatopera.bot.exception.*;
import com.chatopera.bot.models.DictWord;
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
     * 获取自定义词典列表
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
     * 获得自定义词典基本信息
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
     * 测试创建自定义词汇表词典
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
     * 创建或更新自定义词条词典的词条
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

}
