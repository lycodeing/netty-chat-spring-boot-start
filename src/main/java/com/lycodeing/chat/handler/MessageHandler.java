package com.lycodeing.chat.handler;

import com.lycodeing.chat.base.Message;

/**
 * @author xiaotianyu
 */
public interface MessageHandler {

    void handleMessage(Message message);

    void sendMessage(String recipientId, Message message);
}
