package com.library.library_management.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.Base64;

@Configuration
public class SecretDecoderConfig {

    @Value("${app.security.encoded-openai}")
    private String encodedOpenAiKey;

    @Value("${app.security.encoded-gmail}")
    private String encodedGmailPassword;

    @PostConstruct
    public void decodeAndSetSecrets() {
        // 1. Decode the OpenAI Key
        byte[] decodedAiBytes = Base64.getDecoder().decode(encodedOpenAiKey);
        String realAiKey = new String(decodedAiBytes);
        
        // Inject it into the System Properties so the OpenAI Service can find it
        System.setProperty("openai.api.key", realAiKey);

        // 2. Decode the Gmail Password
        byte[] decodedGmailBytes = Base64.getDecoder().decode(encodedGmailPassword);
        String realGmailPassword = new String(decodedGmailBytes);
        
        // Inject it into System Properties so Spring Mail can find it
        System.setProperty("spring.mail.password", realGmailPassword);
        
        System.out.println("✅ Secrets decoded and injected successfully!");
    }
}