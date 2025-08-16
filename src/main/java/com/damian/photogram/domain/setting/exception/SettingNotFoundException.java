package com.damian.photogram.domain.setting.exception;

import com.damian.photogram.core.exception.ApplicationException;

public class SettingNotFoundException extends ApplicationException {
    public SettingNotFoundException(String message) {
        super(message);
    }
}
