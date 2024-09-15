package com.lycodeing.chat.core;

import com.lycodeing.chat.handler.OfflineMessageHandler;
import com.lycodeing.chat.handler.WebSocketAuthServerHandler;
import com.lycodeing.chat.service.AuthenticationService;
import com.lycodeing.chat.handler.WebSocketServerHandler;
import com.lycodeing.chat.processor.MessageProcessor;
import com.lycodeing.chat.properties.WebSocketProperties;
import com.lycodeing.chat.service.OfflineMessageService;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author xiaotianyu
 */
public class NettyWebSocketServer {

    private static final Logger logger = LoggerFactory.getLogger(NettyWebSocketServer.class);

    private final WebSocketProperties properties;
    private final AuthenticationService authenticationService;
    private final MessageProcessor messageProcessor;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    private final OfflineMessageService offlineMessageService;

    public NettyWebSocketServer(WebSocketProperties properties,
                                AuthenticationService authenticationService,
                                MessageProcessor messageProcessor,
                                OfflineMessageService offlineMessageService) {
        this.properties = properties;
        this.authenticationService = authenticationService;
        this.messageProcessor = messageProcessor;
        this.offlineMessageService = offlineMessageService;
    }

    @PostConstruct
    public void start() {
        Thread thread = new Thread(() -> {
            bossGroup = new NioEventLoopGroup(properties.getBossThreadCount());
            workerGroup = new NioEventLoopGroup(properties.getWorkerThreadCount());

            try {
                ServerBootstrap b = new ServerBootstrap();
                b.option(ChannelOption.SO_BACKLOG, 128)
                        .childOption(ChannelOption.SO_KEEPALIVE, true)
                        .childOption(ChannelOption.TCP_NODELAY, true)
                        .group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            public void initChannel(SocketChannel ch) {
                                ChannelPipeline pipeline = ch.pipeline();
                                // http编解码器
                                pipeline.addLast(new HttpServerCodec());
                                // http消息聚合处理器 1MB
                                pipeline.addLast(new HttpObjectAggregator(1024 * 1024));
                                pipeline.addLast(new LoggingHandler(LogLevel.DEBUG));
                                // 认证处理器
                                pipeline.addLast(new WebSocketAuthServerHandler(authenticationService, properties.getAuthParam()));
                                // WebSocket协议处理器
                                pipeline.addLast(new WebSocketServerProtocolHandler(properties.getEndpointName(), null, true, 6666666, false, true));
                                // 离线消息处理器 必须在WebSocket协议处理器后面
                                pipeline.addLast(new OfflineMessageHandler(offlineMessageService));
                                // 心跳处理器
                                pipeline.addLast(new IdleStateHandler(properties.getHeartbeatTimeout(), 0, 0));
                                // 消息处理器
                                pipeline.addLast(new WebSocketServerHandler(messageProcessor));
                            }
                        });
                b.bind(properties.getPort())
                        .sync()
                        .channel()
                        .closeFuture()
                        .sync();
            } catch (Exception e) {
                logger.error("Netty WebSocket Server start error", e);
            } finally {
                bossGroup.shutdownGracefully();
                workerGroup.shutdownGracefully();
            }
        });
        thread.setName("NettyWebSocketServer");
        thread.start();
        logger.info("Netty WebSocket Server started on port " + properties.getPort());
    }

    @PreDestroy
    public void stop() {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }
}
