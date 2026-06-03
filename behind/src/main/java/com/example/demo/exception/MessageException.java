package com.example.demo.exception;

public class MessageException extends BaseException {
    public MessageException(String message) {
        super(message,1002);
    }
}
