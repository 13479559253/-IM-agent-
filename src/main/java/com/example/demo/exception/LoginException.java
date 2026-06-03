package com.example.demo.exception;

public class LoginException extends BaseException {
    public LoginException(String message) {
        super(message,1003);
    }
}
