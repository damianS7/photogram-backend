package com.damian.photogram.core;

import com.damian.photogram.core.service.EmailSenderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class EmailSenderIntegrationTest {

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