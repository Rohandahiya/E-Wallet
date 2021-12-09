package com.example.majorproject;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;

import java.util.Properties;

@Configuration
public class WalletConfig {

    // To get connection to the redis
    @Bean
    LettuceConnectionFactory getRedisFactory(){

        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration("redis-13330.c264.ap-south-1-1.ec2.cloud.redislabs.com",13330);

        //Since no constructor having password so password is entered seperately
        redisStandaloneConfiguration.setPassword("M4GZzWHMRrj5LrOe3BhvQ1F1IarN90dz");

        LettuceConnectionFactory lettuceConnectionFactory = new LettuceConnectionFactory(redisStandaloneConfiguration);

        return  lettuceConnectionFactory;
    }

    // To get the template using which we will perform redis related ops
    @Bean// neccessary to be a bean
    RedisTemplate<String,Object> getTemplate(){

        RedisTemplate<String,Object> redisTemplate = new RedisTemplate<>();

        RedisSerializer<String> stringRedisSerializer = new StringRedisSerializer();
        redisTemplate.setKeySerializer(stringRedisSerializer);

        JdkSerializationRedisSerializer jdkSerializationRedisSerializer = new JdkSerializationRedisSerializer();
        redisTemplate.setValueSerializer(jdkSerializationRedisSerializer);
        redisTemplate.setHashValueSerializer(jdkSerializationRedisSerializer);
        redisTemplate.setConnectionFactory(getRedisFactory());


        return redisTemplate;
    }

    // Configuration of KAFKA for publishing records to kafka

    @Bean// Not Neccessary A Bean
    Properties getKafkaProps(){
        Properties properties = new Properties();

        //HIGHLIGHT: Not required for consumer services
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,"localhost:9092");
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class); // Key is string
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,StringSerializer.class);// Value is also recommended to be String


        // For Consumer , since consumer need to deserialize the keys we have to specify deserializers
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,"localhost:9092");
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringSerializer.class); // Key is string
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,StringSerializer.class);

        return properties;
    }

    @Bean // Not neccessary be a bean
    ProducerFactory<String,String> getProducerFactory(){
                return new DefaultKafkaProducerFactory(getKafkaProps());
    }

    @Bean//Neccessary
    KafkaTemplate<String,String> getKafkaTemplate(){
                return new KafkaTemplate(getProducerFactory());
    }

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
    ObjectMapper getObjectMapper(){
        return new ObjectMapper();
    }


}
