package ru.vershinin.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.vershinin.dto.MailMessageDto;
import ru.vershinin.service.EmailService;

@RestController
@RequestMapping("/v1")
public class MailController {

    @Value("${app.mail.to}")
    private String mailTo;

    private final Logger log = LoggerFactory.getLogger(MailController.class);

    private final EmailService emailService;

    public MailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping(value = "/send")
    public ResponseEntity<String> mailSend(@RequestBody MailMessageDto message) {
        try {
            emailService.sendMail(mailTo, message.getNameTopic(), message.getMessage());

            return new ResponseEntity("message was sent successfully",HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage());
            return new ResponseEntity(e.getLocalizedMessage(), HttpStatus.BAD_REQUEST);
        }

    }


}
