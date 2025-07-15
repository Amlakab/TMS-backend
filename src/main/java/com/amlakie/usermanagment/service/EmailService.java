package com.amlakie.usermanagment.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    // ✅ Injecting the from address (same as spring.mail.username)
    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOtpEmail(String to, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            // ✅ Set the authenticated sender
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("Your OTP Code");
            helper.setText("Your OTP code is: " + otp);

            mailSender.send(message);
            System.out.println("✅ OTP email sent to " + to);
        } catch (MessagingException e) {
            System.err.println("❌ Failed to send OTP email: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
