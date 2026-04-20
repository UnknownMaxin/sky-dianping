package com.maxin.exception;

import com.maxin.result.Result;

public class DeleteBlogImgException extends BaseException {

    public DeleteBlogImgException(String message) {
        super(message);
        Result.error(message);
    }
}
