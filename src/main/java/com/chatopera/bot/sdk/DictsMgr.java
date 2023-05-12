package com.chatopera.bot.sdk;

import com.chatopera.bot.exception.ChatbotException;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.MalformedURLException;

/**
 * 词典管理
 */
public class DictsMgr {

    private Chatbot chatbot;

    // 不支持定义空实例
    private DictsMgr() {

    }

    public DictsMgr(final String clientId, final String clientSecret, final String baseUrl) throws MalformedURLException, ChatbotException {
        this.chatbot = new Chatbot(clientId, clientSecret, baseUrl);
    }

    public DictsMgr(final String clientId, final String clientSecret) throws MalformedURLException, ChatbotException {
        this.chatbot = new Chatbot(clientId, clientSecret);
    }


    /**
     * Get Custom Vocab Dicts by page
     * 支持分页：page 第几页，从 1 开始；limit 每页数据条数
     *
     * @param page  Page
     * @param limit Page size, records in page
     * @return
     */
    public Response getCustomDicts(int page, int limit) throws ChatbotException {
        if (page <= 0) {
            page = 1;
        }

        if (limit <= 0) {
            limit = 20;
        }

        Response resp = this.chatbot.command("GET", String.format("/clause/customdicts?page=%d&limit=%d", page, limit));

        if (resp == null || resp.getRc() != 0) {
            throw new ChatbotException(String.format("Invalid response data[%s]", StringUtils.isNotBlank(resp.getError()) ? resp.getError() : ""));
        }

        return resp;
    }

    ;

    public Chatbot getChatbot() {
        return this.chatbot;
    }


    /**
     * Remove custom dict by name
     * @param dictname 词典标识名
     * @return true, 删除成功；false，删除失败
     * @throws ChatbotException
     */
    public boolean deleteCustomDict(final String dictname) throws ChatbotException {

        Response resp = this.chatbot.command("DELETE", String.format("/clause/customdicts/%s", dictname));

        if (resp.getRc() != 0) {
            System.out.println(String.format("[deleteCustomDict] Invalid response data[%s], dictname %s", StringUtils.isNotBlank(resp.getError()) ? resp.getError() : "", dictname));
            return false;
        }

        return true;
    }
}
