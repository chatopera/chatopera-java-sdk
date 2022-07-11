/*
 * Copyright (C) 2018-2021 Chatopera Inc, <https://www.chatopera.com>
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
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Response {
    private int rc;                // 返回值 code
    private String msg;            // 返回值 消息
    private String error;          // 返回值 错误
    private JSONObject dataObj;
    private JSONArray dataArray;
    private int total = -1;             // 分页，所有数据记录条数
    private int current_page = -1;      // 分页，当前页码，（分页从1开始）
    private int total_page = -1;        // 分页，所有页数

    public final static String DATA_JSON_ARRAY = "JSONArray";
    public final static String DATA_JSON_OBJECT = "JSONObject";
    public final static String DATA_NULL = "null";

    public int getRc() {
        return rc;
    }

    public void setRc(int rc) {
        this.rc = rc;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    /**
     * 返回Data数据
     *
     * @return
     */
    public Object getData() {
        String dataType = getDataType();
        if (StringUtils.equals(dataType, DATA_JSON_ARRAY)) {
            return dataArray;
        } else if (StringUtils.equals(dataType, DATA_JSON_OBJECT)) {
            return dataObj;
        } else {
            return null;
        }
    }

    /**
     * 返回 Data 的对象类型
     *
     * @return
     */
    public String getDataType() {
        if (dataArray != null) {
            return DATA_JSON_ARRAY;
        } else if (dataObj != null) {
            return DATA_JSON_OBJECT;
        } else {
            return DATA_NULL;
        }
    }

    /**
     * 设置data字段
     *
     * @param data
     * @param <T>  JSONObject, JSONArray, String
     * @throws ChatbotException
     */
    public <T> void setData(final T data) throws ChatbotException {

        if (data instanceof String) {
            String data2 = (String) data;
            try {
                if (StringUtils.startsWith(data2, "[")) {
                    if (dataObj == null) {
                        this.dataArray = new JSONArray(data);
                    } else {
                        throw new ChatbotException("Invalid data operations for data property.");
                    }
                } else if (StringUtils.startsWith(data2, "{")) {
                    if (dataArray == null) {
                        this.dataObj = new JSONObject(data);
                    } else {
                        throw new ChatbotException("Invalid data operations for data property.");
                    }
                }
            } catch (JSONException err) {
                throw new ChatbotException("Unable to parser input as JSON.");
            }
        } else if (data instanceof JSONObject) {
            if (dataArray == null) {
                this.dataObj = (JSONObject) data;
            } else {
                throw new ChatbotException("Invalid data operations for data property.");
            }
        } else if (data instanceof JSONArray) {
            if (dataObj == null) {
                this.dataArray = (JSONArray) data;
            } else {
                throw new ChatbotException("Invalid data operations for data property.");
            }
        } else {
            throw new ChatbotException("Unknown class type as JSON.");
        }
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getCurrent_page() {
        return current_page;
    }

    public void setCurrent_page(int current_page) {
        this.current_page = current_page;
    }

    public int getTotal_page() {
        return total_page;
    }

    public void setTotal_page(int total_page) {
        this.total_page = total_page;
    }

    /**
     * response数据转为JSONObject
     *
     * @return
     */
    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();
        obj.put("rc", this.rc);
        obj.put("data", getData());

        if (StringUtils.isNotBlank(msg)) {
            obj.put("msg", msg);
        }

        if (StringUtils.isNotBlank(error)) {
            obj.put("error", error);
        }

        // 增加分类信息
        if (total >= 0) {
            obj.put("total", total);
        }

        if (current_page >= 0) {
            obj.put("current_page", current_page);
        }

        if (total_page >= 0) {
            obj.put("total_page", total_page);
        }

        return obj;
    }
}
