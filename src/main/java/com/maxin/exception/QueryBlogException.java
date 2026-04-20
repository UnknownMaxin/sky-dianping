package com.maxin.exception;

import com.maxin.result.Result;

public class QueryBlogException extends BaseException {

    public QueryBlogException(String message) {
        super(message);
        Result.error(message);
    }
}
