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
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author xiaotianyu
 */
@Configuration
@EnableConfigurationProperties(WebSocketProperties.class)
public class NettyWebSocketAutoConfiguration {

    private final static Logger logger = org.slf4j.LoggerFactory.getLogger(NettyWebSocketAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean(MessageHandlerRegistry.class)
    public MessageHandlerRegistry defaultMessageHandlerRegistry() {
        return MessageHandlerRegistry.INSTANCE;
    }



    @Bean
    @ConditionalOnMissingBean(MessageProcessor.class)
    public MessageProcessor messageProcessor() {
        return (message, registry) -> {
            MessageHandler messageHandler = registry.getMessageHandler(message.getType());
            if (messageHandler != null) {
                messageHandler.handleMessage(message);
            } else {
                logger.warn("message type:{} not found", message.getType());
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean(MessageSenderService.class)
    public MessageSenderService messageSenderService(MessageHandlerRegistry registry) {
        return (recipientId, message) -> {
            MessageHandler handler = registry.getMessageHandler(message.getType());
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
                return offlineMessages.remove(userId);
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
                return null;
            }
        };
    }


    @Bean
    @ConditionalOnMissingBean(NettyWebSocketServer.class)
    public NettyWebSocketServer nettyWebSocketServer(WebSocketProperties properties,
                                                     AuthenticationService authenticationService,
                                                     MessageHandlerRegistry registry,
                                                     MessageProcessor messageProcessor,
                                                     OfflineMessageService offlineMessageService) {
        return new NettyWebSocketServer(properties, authenticationService, registry, messageProcessor, offlineMessageService);
    }
}
