package com.example.dm.util;

public class Result {
    private Integer code;
    private String message;
    private String msg;
    private Object data;
    
    public Result() {
    }
    
    public Result(Integer code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.msg = message;
        this.data = data;
    }
    
    public static Result success() {
        return new Result(200, "success", null);
    }
    
    public static Result success(Object data) {
        return new Result(200, "success", data);
    }
    
    public static Result error() {
        return new Result(500, "error", null);
    }
    
    public static Result error(String message) {
        return new Result(500, message, null);
    }
    
    public static Result error(Integer code, String message) {
        return new Result(code, message, null);
    }
    
    // getter and setter methods
    public Integer getCode() {
        return code;
    }
    
    public void setCode(Integer code) {
        this.code = code;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
        this.msg = message;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
        this.message = msg;
    }
    
    public Object getData() {
        return data;
    }
    
    public void setData(Object data) {
        this.data = data;
    }
}
