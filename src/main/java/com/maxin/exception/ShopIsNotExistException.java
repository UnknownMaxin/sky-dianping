package com.maxin.exception;

import com.maxin.result.Result;

public class ShopIsNotExistException extends BaseException {

    public ShopIsNotExistException(String message) {
        super(message);
        Result.error(message);
    }
}
