package com.lycodeing.chat.handler;

import com.lycodeing.chat.core.ConnectionManager;
import com.lycodeing.chat.event.OfflineMessageEvent;
import com.lycodeing.chat.exceptions.AuthException;
import com.lycodeing.chat.exceptions.UserIdNotFoundException;
import com.lycodeing.chat.service.AuthenticationService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class WebSocketAuthServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static final Logger log = LoggerFactory.getLogger(WebSocketAuthServerHandler.class);

    private final AuthenticationService authenticationService;

    // 配置认证子段
    private final String authParam;

    public WebSocketAuthServerHandler(AuthenticationService authenticationService, String authParam) {
        this.authenticationService = authenticationService;
        this.authParam = authParam;
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest fullHttpRequest) throws Exception {
        log.info("WebSocketAuthServerHandler channelRead0");
        String uri = fullHttpRequest.uri();

        // 解析查询参数
        QueryStringDecoder queryStringDecoder = new QueryStringDecoder(uri);
        Map<String, List<String>> parameters = queryStringDecoder.parameters();
        String token = parameters.get(authParam).stream().findFirst().orElseThrow(() -> new AuthException("认证参数获取错误,请检查配置是否有误,认证参数字段:" + authParam));
        // 认证
        handleAuthentication(ctx, token);

        fullHttpRequest.retain();
        ctx.fireChannelRead(fullHttpRequest);

    }


    private void handleAuthentication(ChannelHandlerContext ctx, String token) {
        if (authenticationService.authenticate(token)) {
            String userId = authenticationService.getUserIdFromToken(token);
            // 判断是否为null,为null则抛出异常
            if (userId == null) {
                throw new UserIdNotFoundException("认证失败");
            }
            registerUser(ctx, userId);
        } else {
            log.warn("Authentication failed for token: {}", token);
            ctx.close(); // 认证失败，关闭连接
        }
    }

    private void registerUser(ChannelHandlerContext ctx, String userId) {
        ConnectionManager.register(userId, ctx.channel());
        log.info("User registered: {}", userId);
    }

}
