package es.amanzag.secretsanta;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EmailNotificationService {
    
    private final static Logger log = LoggerFactory.getLogger(EmailNotificationService.class);

    @Value("${mail.fromAddress:no-reply@noreply.com}") private String fromAddress;
    @Value("${smtp.user:user}") private String smtpUser;
    @Value("${smtp.password:password}") private String smtpPassword;
    @Value("${smtp.host:smtp.gmail.com}") private String smtpHost;
    @Value("${smtp.port:587}") private int smtpPort;
    @Value("${application.hostname}") private String appHost;
    @Value("${mail.enabled:true}") private boolean mailEnabled;
    
    private final static String SUBJECT = "Sorpresa!";
    private final static String BODY_TEMPLATE = "Hay un <a href='%s'>mensaje<a> para ti.";
    private final static String LINK_TEMPLATE = "http://%s/secret-santa/%s/users/%s?token=%s";

    public void notify(User u) throws AddressException, MessagingException {
        Properties props = System.getProperties();
        props.put("mail.transport.protocol", "smtps");
        props.put("mail.smtp.port", smtpPort); 
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");

        Session session = Session.getDefaultInstance(props);
        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(fromAddress));
        msg.setRecipient(Message.RecipientType.TO, new InternetAddress(u.getEmail()));
        msg.setSubject(SUBJECT);
        String content = String.format(BODY_TEMPLATE, generateLink(u));
        msg.setContent(content,"text/html");

        Transport transport = session.getTransport();
        try {
            log.info("Sending message to user {} ({}). {}", u.getId(), u.getEmail(), content);
            if(mailEnabled) {
                transport.connect(smtpHost, smtpUser, smtpPassword);
                transport.sendMessage(msg, msg.getAllRecipients());
            }
        } finally {
            transport.close();          
        }

    }

    private String generateLink(User u) {
        return String.format(LINK_TEMPLATE, appHost, u.getGame().getId(), u.getId(), u.getToken().toString());
    }

}
