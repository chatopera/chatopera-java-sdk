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
package com.chatopera.bot.basics;

import com.chatopera.bot.exception.ChatbotException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.RandomStringUtils;
import org.json.JSONObject;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.HashMap;
import java.util.Map;

/**
 * 凭证管理
 */
public class Credentials {

    private String clientId;
    private String clientSecret;

    // 私有化无参数的构造函数，不支持建立空实例
    private Credentials() {
    }

    public Credentials(final String clientId, final String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    private String byte2hex(byte[] b) {
        StringBuilder hs = new StringBuilder();
        String stmp;
        for (int n = 0; b != null && n < b.length; n++) {
            stmp = Integer.toHexString(b[n] & 0XFF);
            if (stmp.length() == 1)
                hs.append('0');
            hs.append(stmp);
        }
        return hs.toString().toLowerCase();
    }

    private String HmacSHA1Encrypt(String encryptText, String encryptKey) throws Exception {
        String MAC_NAME = "HmacSHA1";
        String ENCODING = "UTF-8";
        byte[] data = encryptKey.getBytes(ENCODING);
        SecretKey secretKey = new SecretKeySpec(data, MAC_NAME);
        Mac mac = Mac.getInstance(MAC_NAME);
        mac.init(secretKey);
        byte[] text = encryptText.getBytes(ENCODING);
        return byte2hex(mac.doFinal(text));
    }


    public String generate(String method, String path) throws ChatbotException {
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        String random = RandomStringUtils.random(10, false, true);
        String signature = null;
        try {
            signature = HmacSHA1Encrypt(this.clientId + timestamp + random + method + path, this.clientSecret);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ChatbotException("生成认证签名异常。");
        }
        Map<String, String> map = new HashMap<String, String>();
        map.put("appId", this.clientId);
        map.put("timestamp", timestamp);
        map.put("random", random);
        map.put("signature", signature);

        JSONObject json = new JSONObject(map);

        return new String(Base64.encodeBase64(json.toString().getBytes()));
    }
}
