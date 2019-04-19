package com.some.kafaka.cofig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class KafkaServiceConfig {

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }

}
