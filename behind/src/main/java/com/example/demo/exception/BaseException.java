package com.example.demo.exception;

import lombok.Getter;

@Getter
public class BaseException extends RuntimeException {
    private final Integer code;
    public BaseException(String message, Integer code) {
        super(message);
        this.code = code;
    }
}
