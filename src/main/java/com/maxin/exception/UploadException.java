package com.maxin.exception;

import com.maxin.result.Result;

public class UploadException extends BaseException {

    public UploadException(String message) {
        super(message);
        Result.error(message);
    }
}
