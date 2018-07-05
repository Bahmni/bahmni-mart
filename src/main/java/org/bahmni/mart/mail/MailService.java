package org.bahmni.mart.mail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
@Profile({"!test"})
public class MailService {

    @Autowired
    private JavaMailSender javaMailSender;

    @Value("#{'${recipients}'.split(',')}")
    private List<String> recipients;

    @Value("${subject}")
    private String subject;

    public void sendJobsStatus(List<String> failedJobs) {
        if (failedJobs.isEmpty() || recipients.isEmpty())
            return;

        SimpleMailMessage mailMessage = new SimpleMailMessage();

        mailMessage.setTo(recipients.toArray(new String[recipients.size()]));

        mailMessage.setSubject(subject + "-" + new Date().toString());
        String body = getBody(failedJobs);
        mailMessage.setText(body);

        javaMailSender.send(mailMessage);
    }

    private String getBody(List<String> failedJobs) {
        StringBuilder mailBody = new StringBuilder();
        mailBody.append("The following jobs are failed, so you may not see up to date data in mart " +
                "database specific to the following.\n\n");
        failedJobs.forEach(failedJob -> mailBody.append(failedJob).append("\n"));
        return mailBody.toString();
    }
}
