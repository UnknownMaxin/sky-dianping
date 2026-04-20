package com.maxin.exception;

import com.maxin.result.Result;

public class WebException extends BaseException {
    public WebException(String message) {
        super(message);
        Result.error(message);
    }
}
