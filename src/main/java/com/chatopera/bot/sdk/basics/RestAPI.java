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
package com.chatopera.bot.sdk.basics;

import kong.unirest.*;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * RestAPI接口
 */
public class RestAPI {

    /**
     * patch headers
     *
     * @param headers
     */
    private static void x(HashMap<String, String> headers) {
        if (headers == null) {
            headers = new HashMap<String, String>();
            headers.put("accept", "application/json");
            return;
        }

        if (!headers.containsKey("Content-Type"))
            headers.put("Content-Type", "application/json");


        if ((!headers.containsKey("accept")) && (!headers.containsKey("Accept")))
            headers.put("Accept", "application/json");
    }


    /**
     * Post
     *
     * @param url
     * @param body
     * @param query
     * @param headers
     * @return
     * @throws UnirestException
     */
    public static JSONObject post(final String url, final JSONObject body, final HashMap<String, Object> query, HashMap<String, String> headers) throws UnirestException {
        HttpRequestWithBody request = Unirest.post(url);
        x(headers);
        // parse response
        HttpResponse<JsonNode> resp = request
                .headers(headers)
                .queryString(query)
                .body(body == null ? "{}" : body.toString())
                .asJson();

        kong.unirest.json.JSONObject obj;
        if (resp.getBody() == null) {
            throw new UnirestException(String.format("Unexpected result with API %s %s, statusText %s", "POST", url, resp.getStatusText()));
        } else {
            obj = resp.getBody().getObject();
            return new JSONObject(obj.toString());
        }
    }

    public static JSONObject post(final String url, final JSONObject body) throws UnirestException {
        return post(url, body, null, null);
    }

    /**
     * Get
     *
     * @param url
     * @param query
     * @param headers
     * @return
     * @throws UnirestException
     */
    public static JSONObject get(final String url, final HashMap<String, Object> query, HashMap<String, String> headers) throws UnirestException {
        GetRequest request = Unirest.get(url);
        x(headers);
        HttpResponse<JsonNode> resp = request
                .headers(headers)
                .queryString(query)
                .asJson();
        // parse response
        kong.unirest.json.JSONObject obj;
        if (resp.getBody() == null) {
            throw new UnirestException(String.format("Unexpected result with API %s %s, statusText %s", "GET", url, resp.getStatusText()));
        } else {
            obj = resp.getBody().getObject();
            return new JSONObject(obj.toString());
        }
    }

    public static JSONObject get(final String url) throws UnirestException {
        return get(url, null, null);
    }

    public static JSONObject get(final String url, HashMap<String, Object> query) throws UnirestException {
        return get(url, query, null);
    }

    public static JSONObject delete(final String url, HashMap<String, String> headers) throws UnirestException {
        x(headers);
        return new JSONObject(Unirest.delete(url).headers(headers).asJson().getBody().getObject().toString());
    }

    public static JSONObject delete(final String url, HashMap<String, String> headers, JSONObject body) throws UnirestException {
        x(headers);
        return new JSONObject(Unirest.delete(url).headers(headers).body(body.toString()).asJson().getBody().getObject().toString());
    }

    public static JSONObject put(final String url, HashMap<String, Object> body, HashMap<String, String> headers) throws UnirestException {
        x(headers);
        return new JSONObject(Unirest.put(url).headers(headers).fields(body).asJson().getBody().getObject().toString());
    }

    /**
     * Put
     *
     * @param url
     * @param body
     * @param query
     * @param headers
     * @return
     * @throws UnirestException
     */
    public static JSONObject put(final String url, final JSONObject body, final HashMap<String, Object> query, HashMap<String, String> headers) throws UnirestException {
        HttpRequestWithBody request = Unirest.put(url);
        x(headers);
        // parse response
        HttpResponse<JsonNode> resp = request
                .headers(headers)
                .queryString(query)
                .body(body.toString())
                .asJson();

        kong.unirest.json.JSONObject obj;
        if (resp.getBody() == null) {
            throw new UnirestException(String.format("Unexpected result with API %s %s, statusText %s", "PUT", url, resp.getStatusText()));
        } else {
            obj = resp.getBody().getObject();
            return new JSONObject(obj.toString());
        }
    }
}
