package com.example.majorproject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final String TOPIC_TRANSACTION_COMPLETE = "transaction_complete";

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    JavaMailSender javaMailSender;

    @Autowired
    SimpleMailMessage simpleMailMessage;

    @KafkaListener(topics = {TOPIC_TRANSACTION_COMPLETE},groupId = "jbdl15")
    private void sendNotification(String message) throws JsonProcessingException {

        JSONObject jsonObject = objectMapper.readValue(message,JSONObject.class);
        String email = (String) jsonObject.get("email");
        String emailMessage = (String) jsonObject.get("message");

        simpleMailMessage.setText(emailMessage);
        simpleMailMessage.setFrom("");
        simpleMailMessage.setTo(email);

        javaMailSender.send(simpleMailMessage);

    }

}
