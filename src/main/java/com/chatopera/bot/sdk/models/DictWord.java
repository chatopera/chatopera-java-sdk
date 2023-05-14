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

package com.chatopera.bot.sdk.models;

import com.chatopera.bot.exception.ResourceInvalidException;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.util.HashSet;

/**
 * 词汇表词典词条
 */
public class DictWord {

    private String word;
    private HashSet<String> synonyms;

    public DictWord(){
    };

    /**
     * @param word 标准词
     * @throws ResourceInvalidException
     */
    public DictWord(String word) throws ResourceInvalidException {
        setWord(word);
    }

    /**
     * @param word 标准词
     * @param synonyms 近义词
     * @throws ResourceInvalidException
     */
    public DictWord(String word, HashSet<String> synonyms) throws ResourceInvalidException {
        setWord(word);
        setSynonyms(synonyms);
    }

    /**
     * @param word 标准词
     * @param synonyms 近义词
     * @throws ResourceInvalidException
     */
    public DictWord(String word, String synonyms) throws ResourceInvalidException {
        setWord(word);
        setSynonyms(convertSynonyms(synonyms));
    }

    public String getWord() {
        return word;
    }

    public void setWord(final String word) throws ResourceInvalidException {
        this.word = StringUtils.trim(word);

        if (StringUtils.isBlank(this.word)) {
            throw new ResourceInvalidException("Invalid dict word data, `word` shoud not be blank");
        }
    }

    public HashSet<String> getSynonyms() {
        return synonyms;
    }

    public void setSynonyms(final HashSet<String> synonyms) {
        this.synonyms = new HashSet<>();

        for (String s : synonyms) {
            s = StringUtils.trim(s);
            addSynonym(s);
        }
    }

    public void addSynonym(final String synonym) {
        if (StringUtils.isNotBlank(synonym)) {
            String t = StringUtils.trim(synonym);
            if (StringUtils.isNotBlank(this.word)) {
                if (StringUtils.equals(this.word, t)) {
                    return;
                }
            }

            if (this.synonyms == null)
                this.synonyms = new HashSet<>();

            this.synonyms.add(t);
        }
    }

    public JSONObject toJson() {
        JSONObject j = new JSONObject();

        if (StringUtils.isNotBlank(this.word)) {
            j.put("word", this.word);
        }

        if (this.synonyms.size() > 0) {
            j.put("synonyms", StringUtils.join(this.synonyms, ";"));
        }

        return j;
    }

    public void fromJson(final JSONObject jsonObject) throws ResourceInvalidException {
        if (jsonObject.has("word")) {
            this.word = StringUtils.trim(jsonObject.getString("word"));
        }

        if (StringUtils.isBlank(this.word)) {
            throw new ResourceInvalidException("Dict object requires `word`");
        }

        if (jsonObject.has("synonyms")) {
            String syns = jsonObject.getString("synonyms");
            setSynonyms(convertSynonyms(syns));
        }
    }

    private HashSet<String> convertSynonyms(final String syns) throws ResourceInvalidException {
        if (StringUtils.isNotBlank(syns)) {
            String[] words = StringUtils.split(syns, ";");
            HashSet<String> synonyms = new HashSet<>();
            for (String w : words) {
                w = StringUtils.trim(w);
                if (StringUtils.isNotBlank(w)) {
                    synonyms.add(w);
                }
            }
            return synonyms;
        }

        throw new ResourceInvalidException("Invalid format of Synonyms string, e.g. `wordA;wordB`");
    }

    /**
     * 将近义词转化为字符串
     * @return
     */
    public String stringifySynonyms(){
        if(this.synonyms != null && this.synonyms.size() > 0){
            return StringUtils.join(this.synonyms, ";");
        }

        return "";
    }


}
