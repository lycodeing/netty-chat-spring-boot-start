package com.lycodeing.chat.enums;

/**
 * @author xiaotianyu
 */

public enum MessageTypeEnum {

    /**
     * 认证
     */
    AUTH("authentication"),

    /**
     * 其他
     */
    OTHER("other");;

    private final String type;

    MessageTypeEnum(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public static MessageTypeEnum getEnum(String type) {
        for (MessageTypeEnum messageTypeEnum : MessageTypeEnum.values()) {
            if (messageTypeEnum.getType().equals(type)) {
                return messageTypeEnum;
            }
        }
        return OTHER;
    }
}
