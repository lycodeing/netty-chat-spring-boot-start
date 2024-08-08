package com.lycodeing.chat.handler;

import com.lycodeing.chat.base.Message;
import com.lycodeing.chat.config.MessageHandlerRegistry;
import com.lycodeing.chat.core.ConnectionManager;
import com.lycodeing.chat.enums.MessageTypeEnum;
import com.lycodeing.chat.exceptions.ParamsException;
import com.lycodeing.chat.processor.MessageProcessor;
import com.lycodeing.chat.service.AuthenticationService;
import com.lycodeing.chat.service.OfflineMessageService;
import com.lycodeing.chat.utils.GsonUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * websocket处理器
 *
 * @author xiaotianyu
 */
public class WebSocketServerHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketServerHandler.class);

    private final static long AUTHENTICATION_TIMEOUT_MILLIS = 5;

    private final AuthenticationService authenticationService;
    private final MessageHandlerRegistry registry;
    private final MessageProcessor messageProcessor;

    private final OfflineMessageService offlineMessageService;


    // 存储 Channel ID 到用户 ID 的映射
    private final ConcurrentMap<String, String> channelIdToUserIdMap = new ConcurrentHashMap<>();

    private final ConcurrentMap<ChannelHandlerContext, ScheduledFuture<?>> authenticationTimeouts = new ConcurrentHashMap<>();

    public WebSocketServerHandler(AuthenticationService authenticationService,
                                  MessageHandlerRegistry registry,
                                  MessageProcessor messageProcessor,
                                  OfflineMessageService offlineMessageService) {
        this.authenticationService = authenticationService;
        this.registry = registry;
        this.messageProcessor = messageProcessor;
        this.offlineMessageService = offlineMessageService;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        logger.info("Connection established: {}", ctx.channel().id().asShortText());
        //  设置认证超时
        ScheduledFuture<?> timeoutFuture = ctx.executor().schedule(() -> {
            if (!channelIdToUserIdMap.containsKey(ctx.channel().id().asShortText())) {
                logger.warn("Authentication timeout for connection: {}", ctx.channel().id().asShortText());
                ctx.close();
                authenticationTimeouts.remove(ctx);
            }
        }, AUTHENTICATION_TIMEOUT_MILLIS, TimeUnit.SECONDS);

        authenticationTimeouts.put(ctx, timeoutFuture);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) {
        Message message = getMessage(msg);
        if (MessageTypeEnum.AUTH.equals(MessageTypeEnum.getEnum(message.getType()))) {
            handleAuthentication(ctx, message);
        } else {
            handleBusinessMessage(ctx, message);
        }

    }

    private static Message getMessage(TextWebSocketFrame msg) {
        String content = msg.text();
        Message message = GsonUtil.fromJson(content, Message.class);
        Optional.ofNullable(message).orElseThrow(() -> new ParamsException("message", "message is empty"));
        Optional.ofNullable(message.getType()).orElseThrow(() -> new ParamsException("type", "type is empty"));
        return message;
    }

    private void handleAuthentication(ChannelHandlerContext ctx, Message message) {
        String token = message.getToken();
        if (authenticationService.authenticate(token)) {
            String userId = authenticationService.getUserIdFromToken(token);
            registerUser(ctx, userId);
            sendOfflineMessages(ctx, userId);
        } else {
            logger.warn("Authentication failed for token: {}", token);
            ctx.close(); // 认证失败，关闭连接
        }
    }

    /**
     * 发送离线消息
     * @param ctx 连接
     * @param userId 用户
     */
    private void sendOfflineMessages(ChannelHandlerContext ctx, String userId) {
        List<Message> offlineMessages = offlineMessageService.getOfflineMessages(userId);
        if(offlineMessages != null && !offlineMessages.isEmpty()){
            for (Message offlineMessage : offlineMessages) {
                ctx.writeAndFlush(new TextWebSocketFrame(GsonUtil.toJson(offlineMessage)));
            }
        }
    }

    private void registerUser(ChannelHandlerContext ctx, String userId) {
        ConnectionManager.register(userId, ctx.channel());
        channelIdToUserIdMap.put(ctx.channel().id().asShortText(), userId);
        logger.info("User registered: {}", userId);
    }

    private void handleBusinessMessage(ChannelHandlerContext ctx, Message message) {
        String userId = channelIdToUserIdMap.get(ctx.channel().id().asShortText());
        if (userId != null) {
            message.setFrom(userId);
            messageProcessor.processMessage(message, registry);
        } else {
            // 未找到用户 ID，说明用户未进行认证，无法处理业务消息
            ctx.close();
        }
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        // 根据 Channel ID 获取用户 ID
        String userId = channelIdToUserIdMap.remove(ctx.channel().id().asShortText());
        if (userId != null) {
            // 注销用户 ID 与 Channel 的关联
            ConnectionManager.unregister(userId);
            logger.info("Connection removed for userId: {}", userId);
        }
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("exceptionCaught", cause);
        if (cause instanceof ParamsException ee) {
            ctx.writeAndFlush(new TextWebSocketFrame(GsonUtil.toJson(ee.getMessage())));
        }
    }
}
