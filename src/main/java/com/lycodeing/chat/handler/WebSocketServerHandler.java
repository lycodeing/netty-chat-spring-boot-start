package com.lycodeing.chat.handler;

import com.lycodeing.chat.base.Message;
import com.lycodeing.chat.core.ConnectionManager;
import com.lycodeing.chat.exceptions.ParamsException;
import com.lycodeing.chat.processor.MessageProcessor;
import com.lycodeing.chat.utils.GsonUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * websocket处理器
 *
 * @author xiaotianyu
 */
public class WebSocketServerHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketServerHandler.class);


    private final MessageProcessor messageProcessor;


    public WebSocketServerHandler(
            MessageProcessor messageProcessor) {
        this.messageProcessor = messageProcessor;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) {
        Message message = getMessage(msg);
        handleBusinessMessage(ctx, message);

    }

    private static Message getMessage(TextWebSocketFrame msg) {
        String content = msg.text();
        Message message = GsonUtil.fromJson(content, Message.class);
        Optional.ofNullable(message).orElseThrow(() -> new ParamsException("message", "message is empty"));
        return message;
    }

    private void handleBusinessMessage(ChannelHandlerContext ctx, Message message) {
        String userId = ConnectionManager.getKey(ctx.channel());
        if (userId != null) {
            message.setFrom(userId);
            messageProcessor.processMessage(message);
        } else {
            // 未找到用户 ID，说明用户未进行认证，无法处理业务消息
            ctx.close();
        }
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        // 根据 Channel ID 获取用户 ID
        String userId = ConnectionManager.getKey(ctx.channel());
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

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("WebSocketServerHandler channelActive");
    }
}
