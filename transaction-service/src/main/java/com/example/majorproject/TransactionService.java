package com.example.majorproject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.UUID;

@Service
public class TransactionService {

    private static final String TOPIC_TRANSACTION_INITIATED =  "transaction_initiated";
    private static final String WALLET_UPDATED = "wallet_update";
    private static final String TOPIC_TRANSACTION_COMPLETE = "transaction_complete";

    @Autowired
    KafkaTemplate<String,String> kafkaTemplate;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    TransactionRepository transactionRepository;

    @Autowired
    RestTemplate restTemplate;

    public String initiateTransaction(TransactionRequest transactionRequest) throws JsonProcessingException {

        Transaction transaction = Transaction.builder()
                                    .amount(transactionRequest.getAmount())
                                    .fromUser(transactionRequest.getFromUser())
                                    .toUser(transactionRequest.getToUser())
                                    .purpose(transactionRequest.getPurpose())
                                    .transactionId(UUID.randomUUID().toString())
                                    .status(TransactionStatus.PENDING)
                                    .build();


        transactionRepository.save(transaction);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("fromUser",transaction.getFromUser());
        jsonObject.put("toUser",transaction.getToUser());
        jsonObject.put("amount",transaction.getAmount());
        jsonObject.put("transactionId",transaction.getTransactionId());

        kafkaTemplate.send(TOPIC_TRANSACTION_INITIATED,objectMapper.writeValueAsString(jsonObject));

        return transaction.getTransactionId();
    }

    @KafkaListener(topics = {WALLET_UPDATED},groupId = "jdbl15")
    public void updateTransaction(String msg) throws JsonProcessingException {
        JSONObject jsonObject = objectMapper.readValue(msg,JSONObject.class);

        String transactionId = (String) jsonObject.get("transactionId");
        String status = (String)jsonObject.get("status");
        String fromUser = (String) jsonObject.get("fromUser");
        String toUser = (String) jsonObject.get("toUser");
        int amount = (int) jsonObject.get("amount");


        TransactionStatus transactionStatus = TransactionStatus.valueOf(status);

        transactionRepository.updateTransactionByStatus(transactionId,transactionStatus);

        // TODO: Send transaction complete to notification service for email generation
        // 1: Get the user email

        //Transaction transaction = transactionRepository.findByTransactionId(transactionId);

        URI uri = URI.create("http://localhost:9000/user?id"+ fromUser);

        JSONObject fromUserDetails = restTemplate.exchange(uri, HttpMethod.GET,new HttpEntity(new HttpHeaders()),JSONObject.class).getBody();




        // 2: Send event to notification service

        JSONObject transactionComplete = new JSONObject();
        transactionComplete.put("email",fromUserDetails.get("email"));
        transactionComplete.put("message","Hey " + fromUser + " your transaction with id:" + transactionId + " got " + transactionStatus.name());

        kafkaTemplate.send(TOPIC_TRANSACTION_COMPLETE,objectMapper.writeValueAsString(transactionComplete));

        if(TransactionStatus.SUCCESS.equals(transactionStatus)){
            URI uri_1 = URI.create("http://localhost:9000/user?id"+ toUser);

            JSONObject toUserDetails = restTemplate.exchange(uri_1, HttpMethod.GET,new HttpEntity(new HttpHeaders()),JSONObject.class).getBody();

            transactionComplete = new JSONObject();
            transactionComplete.put("email",toUserDetails.get("email"));
            transactionComplete.put("message","Hey " + toUser + " you recieved amount of " + amount + "from user:" + fromUserDetails.get("name"));

            kafkaTemplate.send(TOPIC_TRANSACTION_COMPLETE,objectMapper.writeValueAsString(transactionComplete));
        }


    }

}
