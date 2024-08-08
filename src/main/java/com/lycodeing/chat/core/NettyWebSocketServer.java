package com.lycodeing.chat.core;

import com.lycodeing.chat.config.MessageHandlerRegistry;
import com.lycodeing.chat.service.AuthenticationService;
import com.lycodeing.chat.handler.WebSocketServerHandler;
import com.lycodeing.chat.processor.MessageProcessor;
import com.lycodeing.chat.properties.WebSocketProperties;
import com.lycodeing.chat.service.OfflineMessageService;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
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
    private final MessageHandlerRegistry registry;
    private final MessageProcessor messageProcessor;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    private final OfflineMessageService offlineMessageService;

    public NettyWebSocketServer(WebSocketProperties properties,
                                AuthenticationService authenticationService,
                                MessageHandlerRegistry registry,
                                MessageProcessor messageProcessor,
                                OfflineMessageService offlineMessageService) {
        this.properties = properties;
        this.authenticationService = authenticationService;
        this.registry = registry;
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
                b.group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            public void initChannel(SocketChannel ch) {
                                ChannelPipeline pipeline = ch.pipeline();
                                pipeline.addLast(new HttpServerCodec());
                                pipeline.addLast(new HttpObjectAggregator(65536));
                                pipeline.addLast(new IdleStateHandler(properties.getHeartbeatTimeout(), 0, 0));
                                pipeline.addLast(new WebSocketServerProtocolHandler(properties.getEndpointName()));
                                pipeline.addLast(new WebSocketServerHandler(authenticationService, registry, messageProcessor, offlineMessageService));
                            }
                        });
                b.bind(properties.getPort()).sync().channel().closeFuture().sync();
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
