package com.maxin.exception;

import com.maxin.result.Result;

public class PhoneInvalidException extends BaseException {

    public PhoneInvalidException(String message) {
        super(message);
        Result.error(message);
    }
}
