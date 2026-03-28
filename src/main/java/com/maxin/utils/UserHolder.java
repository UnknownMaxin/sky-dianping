package com.maxin.utils;

import com.maxin.dto.UserDTO;

public class UserHolder {
    private static final ThreadLocal<UserDTO> userHolder = new ThreadLocal<>();

    public static void saveUser(UserDTO userDTO) {
        userHolder.set(userDTO);
    }

    public static UserDTO getUser() {
        return userHolder.get();
    }

    public static void removeUser() {
        userHolder.remove();
    }
}
