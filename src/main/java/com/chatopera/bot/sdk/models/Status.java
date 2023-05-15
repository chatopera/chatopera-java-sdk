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
import org.json.JSONObject;

/**
 * 机器人状态
 */
public class Status {

    /**
     * https://gitlab.chatopera.com/chatopera/chatopera.bot/issues/554
     * 状态的定义
     * | rc | 含义 |
     * | --- | --- |
     * | 0 | 模型完成构建 |
     * | 1 | 参数错误，比如没有传chabotID，在dashboard中调用不可能返回这个错误 |
     * | 2 | 正在训练中 |
     * | 3 | 训练失败，此处没有给出原因 |
     * | 4 | 训练失败，没有训练内容，比如没有意图数据等 |
     * | 5 | 训练已经取消 |
     * | 6 | 未知状态，不清楚机器人训练情况，应该是存在其它BUG或运维事故导致，比如 redis 服务不可用 |
     */
    // 意图识别训练状态
    private int retrain;
    // 支持库重新索引状态
    private int reindex;
    // 多轮对话重新解析状态
    private int reparse;

    private String reparseMsg;
    private String retrainMsg;
    private String reindexMsg;

    private String reindexUpdateAt;
    private String reparseUpdateAt;
    private String retrainUpdateAt;

    public Status(final JSONObject json) throws ResourceInvalidException {
        this.retrain = json.optInt("retrain", -1);
        this.reindex = json.optInt("reindex", -1);
        this.reparse = json.optInt("reparse", -1);
        this.reindexUpdateAt = json.optString("reindexUpdateAt");
        this.reparseUpdateAt = json.optString("reparseUpdateAt");
        this.retrainUpdateAt = json.optString("retrainUpdateAt");
        this.reparseMsg = json.optString("reparseMsg");
        this.retrainMsg = json.optString("retrainMsg");
        this.reindexMsg = json.optString("reindexMsg");

        if (!this.check()) {
            throw new ResourceInvalidException("Invalid data value");
        }
    }

    /**
     * 检查数据合法性
     *
     * @return 是否有效
     */
    private boolean check() {
        if (this.retrain == -1 || this.reindex == -1 || this.reparse == -1) {
            return false;
        }
        return true;
    }


    public int getRetrain() {
        return retrain;
    }

    public void setRetrain(int retrain) {
        this.retrain = retrain;
    }

    public int getReindex() {
        return reindex;
    }

    public void setReindex(int reindex) {
        this.reindex = reindex;
    }

    public int getReparse() {
        return reparse;
    }

    public void setReparse(int reparse) {
        this.reparse = reparse;
    }

    public String getReparseMsg() {
        return reparseMsg;
    }

    public void setReparseMsg(String reparseMsg) {
        this.reparseMsg = reparseMsg;
    }

    public String getRetrainMsg() {
        return retrainMsg;
    }

    public void setRetrainMsg(String retrainMsg) {
        this.retrainMsg = retrainMsg;
    }

    public String getReindexMsg() {
        return reindexMsg;
    }

    public void setReindexMsg(String reindexMsg) {
        this.reindexMsg = reindexMsg;
    }

    public String getReindexUpdateAt() {
        return reindexUpdateAt;
    }

    public void setReindexUpdateAt(String reindexUpdateAt) {
        this.reindexUpdateAt = reindexUpdateAt;
    }

    public String getReparseUpdateAt() {
        return reparseUpdateAt;
    }

    public void setReparseUpdateAt(String reparseUpdateAt) {
        this.reparseUpdateAt = reparseUpdateAt;
    }

    public String getRetrainUpdateAt() {
        return retrainUpdateAt;
    }

    public void setRetrainUpdateAt(String retrainUpdateAt) {
        this.retrainUpdateAt = retrainUpdateAt;
    }

}
