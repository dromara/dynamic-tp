package com.dtp.starter.adapter.liteflow.autoconfigure;

import cn.dtp.adapter.liteflow.LiteflowDtpAdapter;
import com.dtp.starter.common.autoconfigure.BaseBeanAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * LiteflowTpAutoConfiguration related
 *
 * @author yanhom
 * @since 1.1.0
 */
@Configuration
@ConditionalOnClass(name = "com.yomahub.liteflow.thread.ExecutorHelper")
@AutoConfigureAfter({BaseBeanAutoConfiguration.class})
public class LiteflowTpAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public LiteflowDtpAdapter liteflowDtpAdapter() {
        return new LiteflowDtpAdapter();
    }
}
