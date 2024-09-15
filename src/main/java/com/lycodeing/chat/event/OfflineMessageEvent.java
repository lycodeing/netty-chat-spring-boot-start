package com.lycodeing.chat.event;

public class OfflineMessageEvent {

    private String userId;


    public OfflineMessageEvent(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }
}
