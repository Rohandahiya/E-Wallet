package com.example.majorproject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class WalletService {

    // Topic name should be the same as that in the producer
    private static final String USER_CREATE_TOPIC = "user_create";
    private static final String TOPIC_TRANSACTION_INITIATED = "transaction_initiated";
    private static final String WALLET_UPDATED = "wallet_update";

    @Autowired
    KafkaTemplate<String,String> kafkaTemplate;

    @Autowired
    WalletRepository walletRepository;

    @Autowired
    ObjectMapper objectMapper;

    // Wallet Service is acting as a consumer
    @KafkaListener(topics = {USER_CREATE_TOPIC},groupId = "jbdl15")
    public void createWallet(String msg) throws JsonProcessingException {        // msg is the message send by the producer
        JSONObject jsonObject = objectMapper.readValue(msg,JSONObject.class);    // Converting message send by the producer into a JSON Object

        String userId = (String)jsonObject.get("userId");
        int amount = (Integer)jsonObject.get("amount");

        Wallet wallet = Wallet.builder()
                        .userId(userId)
                        .balance(amount)
                        .build();

        walletRepository.save(wallet);

    }

    //Update Wallet because of a transaction
    @KafkaListener(topics={TOPIC_TRANSACTION_INITIATED},groupId = "jbdl15")
    public void updateWallet(String msg) throws JsonProcessingException {
        JSONObject jsonObject = objectMapper.readValue(msg,JSONObject.class);

        String fromUser = (String) jsonObject.get("fromUser");
        String toUser = (String) jsonObject.get("toUser");
        int amount = (int) jsonObject.get("amount");
        String transactionId = (String)jsonObject.get("transactionId");

        JSONObject transactionUpdateRequest  = new JSONObject();
        transactionUpdateRequest.put("transactionId",transactionId);
        transactionUpdateRequest.put("fromUser",fromUser);
        transactionUpdateRequest.put("toUser",toUser);
        transactionUpdateRequest.put("amount",amount);

        Wallet fromWallet = walletRepository.findByUserId(fromUser);
        if(fromWallet==null || fromWallet.getBalance() - amount <0){
            transactionUpdateRequest.put("status","FAILED");
        }else {

            walletRepository.updateWallet(fromUser, 0-amount);
            walletRepository.updateWallet(toUser, amount);
            transactionUpdateRequest.put("status","SUCCESS");
        }

        kafkaTemplate.send(WALLET_UPDATED,objectMapper.writeValueAsString(transactionUpdateRequest));
    }


}

