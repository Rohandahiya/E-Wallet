package com.example.majorproject;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class NotificationConfig {

    @Bean
// Not Neccessary A Bean
    Properties getKafkaProps(){
        Properties properties = new Properties();

//        //HIGHLIGHT: Not required for consumer services
//        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,"127.0.0.1:9092");
//        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class); // Key is string
//        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,StringSerializer.class);// Value is also recommended to be String


        // For Consumer , since consumer need to deserialize the keys we have to specify deserializers
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,"127.0.0.1:9092");
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringSerializer.class); // Key is string
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,StringSerializer.class);

        return properties;
    }

//    @Bean // Not neccessary be a bean
//    ProducerFactory<String,String> getProducerFactory(){
//        return new DefaultKafkaProducerFactory(getKafkaProps());
//    }
//
//    @Bean//Neccessary
//    KafkaTemplate<String,String> getKafkaTemplate(){
//        return new KafkaTemplate(getProducerFactory());
//    }

    @Bean
    ConsumerFactory<String,String> getConsumerFactory(){
        return new DefaultKafkaConsumerFactory(getKafkaProps());
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<String ,String> concurrentKafkaListenerContainerFactory(){
        ConcurrentKafkaListenerContainerFactory<String,String> concurrentKafkaListenerContainerFactory = new ConcurrentKafkaListenerContainerFactory<>();
        concurrentKafkaListenerContainerFactory.setConsumerFactory(getConsumerFactory());
        return concurrentKafkaListenerContainerFactory;
    }

    @Bean
    ObjectMapper getobjectmapper(){
        return new ObjectMapper();
    }

    // Configuration required for sending mails
    // smtp host - Simple mail transfer protocal
    @Bean
    JavaMailSender getMailSender(){
        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
        javaMailSender.setHost("smtp.google.com");   // need to specify the smtp host we are using to send mails
        javaMailSender.setPort(587);         // Port for smtp server is 587
        javaMailSender.setUsername("");
        javaMailSender.setPassword("");

        Properties properties = javaMailSender.getJavaMailProperties();
        properties.put("mail.debug",true);
        properties.put("mail.smtp.starttls.enable",true);  // This enable out java application to send mails not otherwise

        return javaMailSender;

    }

    @Bean
    SimpleMailMessage getMailMessage(){
        return new SimpleMailMessage();
    }

}
