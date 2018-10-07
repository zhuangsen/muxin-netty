package com.zs.netty;

import java.io.Serializable;

/**
 * @auther: madison
 * @date: 2018-10-06 23:07
 * @description:
 */
public class DataContent implements Serializable {

    private Integer action;         // 动作类型
    private ChatMessage chatMsg;    //用户的聊天内容entity
    private String extand;          //扩展字段

    public Integer getAction() {
        return action;
    }

    public void setAction(Integer action) {
        this.action = action;
    }

    public ChatMessage getChatMsg() {
        return chatMsg;
    }

    public void setChatMsg(ChatMessage chatMsg) {
        this.chatMsg = chatMsg;
    }

    public String getExtand() {
        return extand;
    }

    public void setExtand(String extand) {
        this.extand = extand;
    }
}
