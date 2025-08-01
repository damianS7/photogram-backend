package com.damian.photogram.setting.exception;

import com.damian.photogram.auth.exception.AuthorizationException;

public class SettingAuthorizationException extends AuthorizationException {
    public SettingAuthorizationException(String message) {
        super(message);
    }
}
