package com.lycodeing.chat.core;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 负责注册和管理 WebSocket 连接的类。
 * @author xiaotianyu
 */
public class ConnectionManager {

    /**
     * 存储连接 ID 到 Channel 的映射。
     */
    private static final Map<String, Channel> CHANNEL_MAP = new ConcurrentHashMap<>();

    // 定义一个 AttributeKey，用于存储 token 信息
    private static final AttributeKey<String> TOKEN_KEY = AttributeKey.valueOf("token");

    /**
     * 注册一个新的连接。
     * @param id 连接的唯一标识符（例如用户 ID 或会话 ID）
     * @param channel Netty 的 Channel 对象
     */
    public static void register(String id, Channel channel) {
        channel.attr(TOKEN_KEY).set(id);
        CHANNEL_MAP.put(id, channel);
    }

    /**
     * 获取与特定 ID 相关联的 Channel。
     * @param id 连接的唯一标识符
     * @return 对应的 Channel，如果不存在则返回 null
     */
    public static Channel getChannelById(String id) {
        return CHANNEL_MAP.get(id);
    }

    /**
     * 取消注册并移除指定 ID 的连接。
     * @param id 连接的唯一标识符
     */
    public static void unregister(String id) {
        CHANNEL_MAP.remove(id);
    }

    /**
     * 获取所有活跃的 Channel。
     * @return 所有活跃的 Channel
     */
    public static Map<String, Channel> getAllChannels() {
        return new ConcurrentHashMap<>(CHANNEL_MAP);
    }

    /**
     * 提供channel 获取key
     */
    public static String getKey(Channel channel) {
        return channel.attr(TOKEN_KEY).get();
    }
}
