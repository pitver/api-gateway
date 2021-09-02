package ru.vershinin.service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.vershinin.dto.MailMessageDto;

@Service
public class KafkaProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }


    /**
     * сервис "producer" - отправка сообщений
     * @param topic - наименование topic Kafka
     * @param message - сообщение
     * @see MailMessageDto
     */
    public void sendMessage(String topic, MailMessageDto message){

        this.kafkaTemplate.send(topic,message);

    }


}
