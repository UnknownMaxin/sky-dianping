package com.maxin.exception;

import com.maxin.result.Result;

public class BaseException extends RuntimeException {

    public BaseException(String message) {
        super(message);
        Result.error(message);
    }
}
