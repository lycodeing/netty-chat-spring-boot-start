package com.lycodeing.chat.processor;

import com.lycodeing.chat.base.Message;
import com.lycodeing.chat.config.MessageHandlerRegistry;
import com.lycodeing.chat.handler.MessageHandler;

/**
 * 消息处理类
 * @author xiaotianyu
 */
public interface MessageProcessor {

    void processMessage(Message message, MessageHandlerRegistry registry);
}
