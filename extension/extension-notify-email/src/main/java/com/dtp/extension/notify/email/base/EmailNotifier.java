package com.dtp.extension.notify.email.base;

import com.dtp.common.dto.NotifyPlatform;
import com.dtp.common.em.NotifyPlatformEnum;
import com.dtp.core.notify.base.Notifier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.annotation.Resource;
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

    @Resource
    private JavaMailSender javaMailSender;
    @Resource
    private TemplateEngine templateEngine;

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
