package com.lycodeing.chat.exceptions;

public class UserIdNotFoundException extends AuthException {
    public UserIdNotFoundException(String message) {
        super(message);
    }
}
