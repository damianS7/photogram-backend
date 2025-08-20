package com.damian.photogram.core;

import com.damian.photogram.core.service.EmailSenderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;

@ExtendWith(MockitoExtension.class)
class EmailSenderServiceTest {
    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailSenderService emailSenderService;

    @Test
    void shouldSendEmail() {
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
        emailSenderService.send(
                "customer@demo.com",
                "hello",
                "hello world"
        );
    }
}