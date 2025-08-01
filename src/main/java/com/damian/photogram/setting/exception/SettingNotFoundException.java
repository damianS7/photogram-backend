package com.damian.photogram.setting.exception;

import com.damian.photogram.common.exception.ApplicationException;

public class SettingNotFoundException extends ApplicationException {
    public SettingNotFoundException(String message) {
        super(message);
    }
}
