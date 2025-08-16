package com.damian.photogram.domain.setting.exception;

import com.damian.photogram.app.auth.exception.AuthorizationException;

public class SettingAuthorizationException extends AuthorizationException {
    public SettingAuthorizationException(String message) {
        super(message);
    }
}
