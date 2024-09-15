package com.lycodeing.chat.config;


import com.lycodeing.chat.base.Message;
import com.lycodeing.chat.core.NettyWebSocketServer;
import com.lycodeing.chat.service.AuthenticationService;
import com.lycodeing.chat.service.MessageSenderService;
import com.lycodeing.chat.handler.MessageHandler;
import com.lycodeing.chat.processor.MessageProcessor;
import com.lycodeing.chat.properties.WebSocketProperties;
import com.lycodeing.chat.service.OfflineMessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author xiaotianyu
 */
@Configuration
@EnableConfigurationProperties(WebSocketProperties.class)
public class NettyWebSocketAutoConfiguration {

    private final static Logger logger = LoggerFactory.getLogger(NettyWebSocketAutoConfiguration.class);


    @Bean
    @ConditionalOnMissingBean(MessageHandlerRegistry.class)
    public MessageHandlerRegistry defaultMessageHandlerRegistry() {
        return MessageHandlerRegistry.INSTANCE;
    }


    @Bean
    @ConditionalOnMissingBean(MessageProcessor.class)
    public MessageProcessor messageProcessor() {
        return message -> {
            MessageHandler messageHandler = defaultMessageHandlerRegistry().getMessageHandler(message.getTarget());
            if (messageHandler != null) {
                messageHandler.handleMessage(message);
            } else {
                logger.warn("message type:{} not found", message.getTarget());
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean(MessageSenderService.class)
    public MessageSenderService messageSenderService(MessageHandlerRegistry registry) {
        return (recipientId, message) -> {
            MessageHandler handler = registry.getMessageHandler(message.getTarget());
            if (handler != null) {
                handler.sendMessage(recipientId, message);
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean(OfflineMessageService.class)
    public OfflineMessageService offlineMessageService() {
        return new OfflineMessageService() {

            private final Map<String, List<Message>> offlineMessages = new ConcurrentHashMap<>();

            @Override
            public void saveOfflineMessage(String userId, Message message) {
                offlineMessages.computeIfAbsent(userId, k -> new ArrayList<>()).add(message);
            }

            @Override
            public List<Message> getOfflineMessages(String userId) {
                List<Message> messages = offlineMessages.get(userId);
                if (messages == null || messages.isEmpty()) {
                    logger.info("userId:{} offline message is empty", userId);
                    return new ArrayList<>();
                }
                return messages;
            }

            @Override
            public void removeOfflineMessage(String userId) {
                offlineMessages.remove(userId);
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean(AuthenticationService.class)
    public AuthenticationService authenticationService() {
        return new AuthenticationService() {
            @Override
            public boolean authenticate(String token) {
                return true;
            }

            @Override
            public String getUserIdFromToken(String token) {
                return UUID.randomUUID().toString().replace("-", "");
            }
        };
    }


    @Bean
    @ConditionalOnMissingBean(NettyWebSocketServer.class)
    public NettyWebSocketServer nettyWebSocketServer(WebSocketProperties properties,
                                                     AuthenticationService authenticationService,
                                                     MessageProcessor messageProcessor,
                                                     OfflineMessageService offlineMessageService) {
        return new NettyWebSocketServer(properties, authenticationService, messageProcessor, offlineMessageService);
    }
}
