package com.maxin.exception;

import com.maxin.result.Result;

public class SaveBlogException extends BaseException {

    public SaveBlogException(String message) {
        super(message);
        Result.error(message);
    }
}
