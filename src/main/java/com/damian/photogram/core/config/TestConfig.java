package com.damian.photogram.core.config;

import net.datafaker.Faker;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestConfig {

    @Bean
    public Faker getFaker() {
        return new Faker();
    }
}

