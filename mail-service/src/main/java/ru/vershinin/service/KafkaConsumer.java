package ru.vershinin.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import ru.vershinin.dto.MailMessageDto;

@Service
public class KafkaConsumer {

    private final Logger log = LoggerFactory.getLogger(KafkaConsumer.class);

    @Value("${app.mail.to}")
    private String mailTo;

    private final EmailService emailService;

    public KafkaConsumer(EmailService emailService) {
        this.emailService = emailService;
    }


    /**
     * стандартный слушатель kafka
     *
     * @param message  - объект сообщения
     */
    @KafkaListener(topics = "${app.topic.example}", groupId = "message_group_id")
    public void consume(MailMessageDto message){
        log.info("Consuming the message: {}" ,message);
        emailService.sendMail(mailTo, message.getNameTopic(), message.getMessage());

    }
}
