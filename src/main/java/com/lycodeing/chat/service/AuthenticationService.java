package com.lycodeing.chat.service;

/**
 * 认证处理
 * @author xiaotianyu
 */
public interface AuthenticationService {

    /**
     * 认证用户是否有效。
     *
     * @param token 用户提供的 token
     * @return 如果认证成功则返回 true，否则返回 false
     */
    boolean authenticate(String token);

    /**
     * 获取用户 ID 根据 token。
     *
     * @param token 用户提供的 token
     * @return 如果 token 有效，返回用户 ID，否则返回 null
     */
    String getUserIdFromToken(String token);
}
