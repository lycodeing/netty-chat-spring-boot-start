package com.lycodeing.chat.config;

import com.lycodeing.chat.enums.MessageTypeEnum;
import com.lycodeing.chat.handler.MessageHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author xiaotianyu
 */
public class MessageHandlerRegistry {

    public static MessageHandlerRegistry INSTANCE = new MessageHandlerRegistry();
    private final Map<String, MessageHandler> handlers =  new ConcurrentHashMap<>();

    private MessageHandlerRegistry() {
    }

    public MessageHandlerRegistry register(String key, MessageHandler handler) {
        handlers.put(key, handler);
        return INSTANCE;
    }
    public void unregister(String key) {
        handlers.remove(key);
    }

    public MessageHandler getMessageHandler(String key) {
        return handlers.get(key);
    }


}
