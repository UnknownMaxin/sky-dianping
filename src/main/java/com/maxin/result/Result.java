package com.maxin.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 后端统一返回结果
 * @param <T>
 */
@Data
public class Result<T> implements Serializable {

    private Integer code; //编码：1成功，0和其它数字为失败
    private String msg; //错误信息
    private T data; //数据

    // 对旧项目进行兼容
    private Boolean success;
    private String errorMsg;
    private Long total;

    public static <T> Result<T> success() {
        Result<T> result = new Result<T>();
        result.code = 1;
        result.success = true;
        return result;
    }

    public static <T> Result<T> success(T object) {
        Result<T> result = new Result<T>();
        result.data = object;
        result.code = 1;
        result.success = true;
        return result;
    }

    public static <T> Result<T> error(String msg) {
        Result result = new Result();
        result.msg = msg;
        result.errorMsg = msg;
        result.code = 0;
        result.success = false;
        return result;
    }
}