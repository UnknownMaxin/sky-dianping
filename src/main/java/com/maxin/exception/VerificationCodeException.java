package com.maxin.exception;

import com.maxin.result.Result;

public class VerificationCodeException extends BaseException {

    public VerificationCodeException(String message) {
        super(message);
        Result.error(message);
    }
}
