package com.damian.photogram.core;

import com.damian.photogram.AbstractIntegrationTest;
import com.damian.photogram.core.service.EmailSenderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class EmailSenderIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private EmailSenderService emailSenderService;

    @Test
    void shouldSendEmail() {
        emailSenderService.send(
                "customer@demo.com",
                "hello",
                "hello world"
        );
    }
}