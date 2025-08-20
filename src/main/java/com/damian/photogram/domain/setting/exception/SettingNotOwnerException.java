package com.damian.photogram.domain.setting.exception;

import com.damian.photogram.core.exception.ApplicationException;

public class SettingNotOwnerException extends ApplicationException {
    public SettingNotOwnerException(String message) {
        super(message);
    }
}
