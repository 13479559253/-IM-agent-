package com.example.demo.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Result<T> {
    private Integer code;
    private String msg;
    private T data;

    public static <T> Result<T> success(T data){
        return new Result<>(1000, "success", data);
    }

    public static Result<?> success(){
        return new Result<>(1000, "success", null);
    }

    public static Result<?> error(Integer code){
        return new Result<>(code, null, null);
    }

    public static Result<?> error(Integer code,String msg){
        return new Result<>(code, msg, null);
    }
}
