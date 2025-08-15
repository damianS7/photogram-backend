package com.damian.photogram.mail;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class EmailTestRunner implements CommandLineRunner {

    private final EmailSenderService emailSenderService;

    public EmailTestRunner(EmailSenderService emailSenderService) {
        this.emailSenderService = emailSenderService;
    }

    @Override
    public void run(String... args) {
        //        emailService.sendEmail("usuario@ejemplo.com", "hola", "texto");
        //        System.out.println("✅ Correo de prueba enviado");
    }
}