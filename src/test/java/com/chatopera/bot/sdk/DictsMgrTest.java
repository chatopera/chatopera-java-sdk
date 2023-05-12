package com.chatopera.bot.sdk;

import com.chatopera.bot.exception.ChatbotException;
import com.chatopera.bot.utils.EnvUtil;
import com.chatopera.bot.utils.Logger;
import junit.framework.TestCase;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Properties;

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

}
