package ru.vershinin.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender javaMailSender;

    public EmailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    /**
     *
     * отправка простого почтового сообщения
     * @param toEmail - адрес назначения
     * @param subject - тема сообщения
     * @param message - тело сообщения
     */
    public void sendMail(String toEmail, String subject, String message) {

        var mailMessage = new SimpleMailMessage();

        mailMessage.setTo(toEmail);
        mailMessage.setSubject(subject);
        mailMessage.setText(message);
        //адрес почтового ящика с которого будет отправлено сообщение
        mailMessage.setFrom("pitver2014@yandex.ru");

        javaMailSender.send(mailMessage);
    }
}
