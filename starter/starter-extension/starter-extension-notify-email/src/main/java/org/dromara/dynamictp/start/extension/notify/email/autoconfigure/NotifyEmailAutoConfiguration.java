package org.dromara.dynamictp.start.extension.notify.email.autoconfigure;


import org.dromara.dynamictp.core.notifier.DtpNotifier;
import org.dromara.dynamictp.extension.notify.email.autoconfigure.DtpEmailNotifier;
import org.dromara.dynamictp.extension.notify.email.autoconfigure.EmailNotifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;

/**
 * NotifyEmailAutoConfiguration related
 *
 * @author ljinfeng
 * @since 1.0.8
 **/
@Configuration
@ConditionalOnProperty(prefix = "spring.mail", name = "host")
public class NotifyEmailAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public EmailNotifier emailNotifier(JavaMailSender javaMailSender, TemplateEngine templateEngine) {
        return new EmailNotifier(javaMailSender, templateEngine);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(EmailNotifier.class)
    public DtpNotifier dtpEmailNotifier() {
        return new DtpEmailNotifier();
    }

}
