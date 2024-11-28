package interestingideas.brainchatserver.service;

import interestingideas.brainchatserver.mail.EmailSender;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@AllArgsConstructor
public class EmailService implements EmailSender {

    private final JavaMailSender javaMailSender;

//    @Value("${spring.mail.password}")
//    private String sendGridKey;
    @Override
    @Async
    public void send(String sender, String to, String email) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
            helper.setText(email);
            helper.setTo(to);
            helper.setSubject("Confirm your email");
            helper.setFrom(sender);
            javaMailSender.send(mimeMessage);
//            Email sender = new Email("janamaxonko2001@gmail.com");
//            Email reciever = new Email(to);
//            Mail mail = new Mail(sender, "Confirm your email", reciever, new Content("text/plain", email));
//            SendGrid sg = new SendGrid(key);
//            Request request = new Request();
//            try {
//                request.setMethod(Method.POST);
//                request.setEndpoint("mail/send");
//                request.setBody(mail.build());
//                Response res = sg.api(request);
//                System.out.println("were here");
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
        } catch (MessagingException e) {
            throw new IllegalStateException("failed to send email");
        }
    }
}
