package com.lycodeing.chat.base;

import java.util.List;
import java.util.Map;

/**
 * 代表通过 WebSocket 发送和接收的消息。
 */
public class Message {


    /**
     * 消息类型，例如文本消息、红包消息等。
     * {@link com.lycodeing.chat.enums.MessageTypeEnum}
     */
    private String type;

    /**
     * 消息发送者的用户ID。
     */
    private String from;

    /**
     * 消息接收者的用户ID。
     */
    private String to;

    /**
     * 接收者的类型，可以是用户或群组等。
     */
    private String target;

    /**
     * 消息的内容，具体的消息数据。
     */
    private Content content;

    /**
     * 消息的时间戳，表示消息的发送时间。
     */
    private long timestamp;

    /**
     * 消息的唯一标识符，用于跟踪消息。
     */
    private String messageId;

    /**
     * 附加的元数据，用于携带其他信息。
     */
    private Map<String, Object> metadata;

    private String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public Content getContent() {
        return content;
    }

    public void setContent(Content content) {
        this.content = content;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    /**
     * 消息内容类，包含具体的消息数据。
     */
    public static class Content {

        /**
         * 文本消息的内容。
         */
        private String text;

        /**
         * 附件列表，包含消息中附带的文件、图片等。
         */
        private List<Attachment> attachments;


        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public List<Attachment> getAttachments() {
            return attachments;
        }

        public void setAttachments(List<Attachment> attachments) {
            this.attachments = attachments;
        }


        @Override
        public String toString() {
            return "Content{" +
                    "text='" + text + '\'' +
                    ", attachments=" + attachments +
                    '}';
        }
    }

    /**
     * 附件类，代表消息中附带的文件或图片等。
     */
    public static class Attachment {

        /**
         * 附件的类型，例如图片、文件等。
         */
        private String type;

        /**
         * 附件的 URL 地址，用于访问附件。
         */
        private String url;

        /**
         * 附件的名称，例如文件名或图片描述。
         */
        private String name;


        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }


        @Override
        public String toString() {
            return "Attachment{" +
                    "type='" + type + '\'' +
                    ", url='" + url + '\'' +
                    ", name='" + name + '\'' +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "Message{" +
                "type='" + type + '\'' +
                ", from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", target='" + target + '\'' +
                ", content=" + content +
                ", timestamp=" + timestamp +
                ", messageId='" + messageId + '\'' +
                ", metadata=" + metadata +
                '}';
    }
}
