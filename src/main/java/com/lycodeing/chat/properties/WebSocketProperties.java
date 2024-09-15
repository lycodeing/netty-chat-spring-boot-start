package com.lycodeing.chat.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author xiaotianyu
 */
@ConfigurationProperties(prefix = "netty.websocket")
public class WebSocketProperties {

    /**
     * 端口
     */
    private int port = 8080;

    /**
     * 认证的参数名称
     */
    private String authParam = "token";

    /**
     * 端点
     */
    private String endpointName = "/ws";

    /**
     * 心跳超时时间
     */
    private int heartbeatTimeout = 60;

    /**
     * boss线程数
     */
    private int bossThreadCount = 1;
    /**
     * worker线程数
     */
    private int workerThreadCount = 4;

    public String getAuthParam() {
        return authParam;
    }

    public void setAuthParam(String authParam) {
        this.authParam = authParam;
    }

    public int getBossThreadCount() {
        return bossThreadCount;
    }

    public void setBossThreadCount(int bossThreadCount) {
        this.bossThreadCount = bossThreadCount;
    }

    public int getWorkerThreadCount() {
        return workerThreadCount;
    }

    public void setWorkerThreadCount(int workerThreadCount) {
        this.workerThreadCount = workerThreadCount;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getEndpointName() {
        return endpointName;
    }

    public void setEndpointName(String endpointName) {
        this.endpointName = endpointName;
    }

    public int getHeartbeatTimeout() {
        return heartbeatTimeout;
    }

    public void setHeartbeatTimeout(int heartbeatTimeout) {
        this.heartbeatTimeout = heartbeatTimeout;
    }
}
