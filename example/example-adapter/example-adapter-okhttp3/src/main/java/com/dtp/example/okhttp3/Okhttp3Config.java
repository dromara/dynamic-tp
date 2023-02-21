package com.dtp.example.okhttp3;

import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Okhttp3Config related
 *
 * @author yanhom
 * @since 1.1.0
 */
@Configuration
public class Okhttp3Config {

    @Bean
    public OkHttpClient okHttpClient() {
        return new OkHttpClient();
    }
}
