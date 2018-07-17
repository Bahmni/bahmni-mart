package org.bahmni.mart.notification;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class MailSender {

    private static final Logger logger = LoggerFactory.getLogger(MailSender.class);

    @Value("${bahmni-mart.mail.subject}")
    private String subject;

    @Value("${bahmni-mart.mail.from}")
    private String from;

    @Value("${bahmni-mart.mail.recipients}")
    private String recipients;

    public void sendMail(List<String> failedJobs) {
        if (recipients.isEmpty() || from.isEmpty() || CollectionUtils.isEmpty(failedJobs)) {
            return;
        }
        String format = getFormatOfMail(failedJobs);
        String sendMailCommand = String.format("echo %s | sendmail -v %s", format, recipients);
        try {
            Runtime.getRuntime().exec(new String[]{"bash", "-c", sendMailCommand});
        } catch (IOException e) {
            String joinedFailedJobs = String.join("\n", failedJobs);
            logger.info(String.format("Can't send the mail for following failed jobs\n%s", joinedFailedJobs));
        }
    }

    private String getFormatOfMail(List<String> failedJobs) {
        String joinedFailedJobs = String.join("\n", failedJobs);
        String body = String.format("These following jobs failed during execution -\n%s\n", joinedFailedJobs);
        return String.format("\"Subject: %s\nFrom: %s\n%s\"", subject, from, body);
    }
}

