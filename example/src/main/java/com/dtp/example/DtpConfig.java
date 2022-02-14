package com.dtp.example;

import com.dtp.core.thread.DtpExecutor;
import com.dtp.core.support.ThreadPoolCreator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * DtpConfig related
 *
 * @author yanhom
 */
@Configuration
public class DtpConfig {

    @Bean
    public DtpExecutor dtpExecutor() {
        return ThreadPoolCreator.createDynamicFast("dynamic-tp-test");
    }
}
