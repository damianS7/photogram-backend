package com.damian.photogram.infrastructure.mail;

import com.damian.photogram.core.AbstractIntegrationTest;
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