package com.maxin.exception;

import com.maxin.result.Result;

public class SeckillVoucherException extends BaseException {

    public SeckillVoucherException(String message) {
        super(message);
        Result.error(message);
    }
}
