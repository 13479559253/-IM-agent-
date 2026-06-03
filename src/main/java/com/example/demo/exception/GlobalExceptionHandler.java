package com.example.demo.exception;

import com.example.demo.pojo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler{
    @ExceptionHandler(BaseException.class)
    public Result<?> RegexExceptionHandler(BaseException e){
        return Result.error(e.getCode(),e.getMessage());
    }
    @ExceptionHandler(Exception.class)
    public Result<?> ExceptionHandler(Exception e){
        log.error(e.getMessage());
        return Result.error(500, e.getMessage());
    }
}
