package com.lycodeing.chat.exceptions;

/**
 * @author xiaotianyu
 */
public class ParamsException extends RuntimeException{

    private String fieldName;

    public ParamsException(String fieldName, String message) {
        super(message);
        this.fieldName = fieldName;
    }

    public ParamsException(String message) {
        super(message);
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }
}
