package org.dromara.dynamictp.extension.notify.email.autoconfigure;

import org.dromara.dynamictp.common.em.NotifyPlatformEnum;
import org.dromara.dynamictp.common.entity.NotifyPlatform;
import org.dromara.dynamictp.core.notifier.base.Notifier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.internet.MimeMessage;
import java.util.Date;

/**
 * EmailNotifier related
 *
 * @author ljinfeng
 * @since 1.0.8
 */
@Slf4j
public class EmailNotifier implements Notifier {

    @Value("${spring.mail.username}")
    private String sendFrom;

    @Value("${spring.mail.title:ThreadPool Notify}")
    private String title;

    private final JavaMailSender javaMailSender;

    private final TemplateEngine templateEngine;

    public EmailNotifier(JavaMailSender javaMailSender, TemplateEngine templateEngine) {
        this.javaMailSender = javaMailSender;
        this.templateEngine = templateEngine;
    }

    @Override
    public String platform() {
        return NotifyPlatformEnum.EMAIL.name().toLowerCase();
    }

    @Override
    public void send(NotifyPlatform platform, String content) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            messageHelper.setSubject(title);
            messageHelper.setFrom(sendFrom);
            messageHelper.setTo(platform.getReceivers().split(","));
            messageHelper.setSentDate(new Date());
            messageHelper.setText(content, true);
            javaMailSender.send(mimeMessage);
            log.info("DynamicTp notify, email send success.");
        } catch (Exception e) {
            log.error("DynamicTp notify, email send failed...", e);
        }
    }

    public String processTemplateContent(String template, Context context) {
        return templateEngine.process(template, context);
    }

}
