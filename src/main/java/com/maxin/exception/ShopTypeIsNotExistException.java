package com.maxin.exception;

import com.maxin.result.Result;

public class ShopTypeIsNotExistException extends BaseException {

    public ShopTypeIsNotExistException(String message) {
        super(message);
        Result.error(message);
    }
}
