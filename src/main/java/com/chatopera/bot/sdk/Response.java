/*
 * Copyright (C) 2018-2020 Chatopera Inc, <https://www.chatopera.com>
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
    private int rc;
    private String msg;
    private String error;
    private JSONObject dataObj;
    private JSONArray dataArray;

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
        if (dataObj != null) {
            return dataObj;
        } else if (dataArray != null) {
            return dataArray;
        } else {
            return null;
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


    /**
     * response数据转为JSONObject
     *
     * @return
     */
    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();
        obj.put("rc", this.rc);
        obj.put("msg", msg);
        obj.put("error", error);
        obj.put("data", getData());
        return obj;
    }
}
