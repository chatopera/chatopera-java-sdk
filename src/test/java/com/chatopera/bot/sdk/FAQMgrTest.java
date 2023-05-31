package com.chatopera.bot.sdk;

import com.chatopera.bot.exception.ChatbotException;
import com.chatopera.bot.sdk.basics.Response;
import com.chatopera.bot.utils.EnvUtil;
import com.chatopera.bot.utils.Logger;
import junit.framework.TestCase;

import java.io.IOException;
import java.net.MalformedURLException;

public class FAQMgrTest extends TestCase {

    private FAQMgr faqMgr;
    private String botClientId;
    private String botClientSecret;

    public void setUp() throws IOException, ChatbotException, MalformedURLException, ChatbotException {
        // 开启 SDK 日志，也可以通过设置环境变量 CHATOPERA_SDK_LOG_TRACE=on|off 来控制
        Logger.setEnabled(true);

        // 从系统环境变量中读取机器人密钥
        botClientId = EnvUtil.getEnv("BOT_CLIENT_ID", "");
        botClientSecret = EnvUtil.getEnv("BOT_CLIENT_SECRET", "");

        this.faqMgr = new FAQMgr(botClientId,
                botClientSecret,
                "https://bot.chatopera.com");
    }


    public void testGetFaqs() throws ChatbotException {
        Response resp = this.faqMgr.getFaqs(1, 10);
        assertEquals(resp.getRc(), 0);
        System.out.println(resp.getData().toString());
    }

    public void testIssue88GetFaqBadCase() throws MalformedURLException, ChatbotException {
        Chatbot c = new Chatbot(botClientId, botClientSecret);

        Response resp = c.command("GET", String.format("/faq/database?q=&&page=%d&limit=%d", 1, 10));
        assertEquals(resp.getRc(), 0);
    }


}
