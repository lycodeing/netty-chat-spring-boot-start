package com.lycodeing.chat.handler;

import com.lycodeing.chat.base.Message;
import com.lycodeing.chat.core.ConnectionManager;
import com.lycodeing.chat.event.OfflineMessageEvent;
import com.lycodeing.chat.service.OfflineMessageService;
import com.lycodeing.chat.utils.GsonUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class OfflineMessageHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(OfflineMessageHandler.class);

    // 服务用来获取离线消息
    private final OfflineMessageService offlineMessageService;

    public OfflineMessageHandler(OfflineMessageService offlineMessageService) {
        this.offlineMessageService = offlineMessageService;
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("OfflineMessageHandler channelActive");
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof WebSocketServerProtocolHandler.HandshakeComplete) {
            // 推送离线消息
            String key = ConnectionManager.getKey(ctx.channel());
            pushOfflineMessages(key, ctx);
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }


    private void pushOfflineMessages(String userId, ChannelHandlerContext ctx) {
        if (userId != null) {
            // 查询用户的离线消息
            List<Message> offlineMessages = offlineMessageService.getOfflineMessages(userId);
            if (offlineMessages != null && !offlineMessages.isEmpty()) {
                // 推送每条离线消息给客户端
                for (Message message : offlineMessages) {
                    ctx.writeAndFlush(new TextWebSocketFrame(GsonUtil.toJson(message)));
                }
                offlineMessageService.removeOfflineMessage(userId);
                logger.info("推送离线消息成功，用户id：{}", userId);
            }
        }
    }
}
