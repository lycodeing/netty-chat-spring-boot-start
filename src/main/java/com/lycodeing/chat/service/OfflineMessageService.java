package com.lycodeing.chat.service;

import com.lycodeing.chat.base.Message;

import java.util.List;

/**
 * 离线消息存储的接口
 *
 * @author xiaotianyu
 */
public interface OfflineMessageService {

    // 保存离线消息
    void saveOfflineMessage(String userId, Message message);

    // 获取信息
    List<Message> getOfflineMessages(String userId);


    void removeOfflineMessage(String userId);
}
