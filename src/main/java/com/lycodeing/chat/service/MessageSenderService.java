package com.lycodeing.chat.service;

import com.lycodeing.chat.base.Message;

/**
 * 主动推送消息类
 * @author xiaotianyu
 */
public interface MessageSenderService {
    void sendMessage(String recipientId, Message message);
}
