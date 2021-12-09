package com.example.majorproject;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.Properties;

@Configuration
public class UserConfiguration {

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

    @Bean // Not Neccessary A Bean
    Properties getKafkaProps(){
        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,"localhost:9092");
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class); // Key is string
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,StringSerializer.class);// Value is also recommended to be String

        return properties;
    }

    @Bean // Not neccessary be a bean
    ProducerFactory<String,String> getProducerFactory(){
        return new DefaultKafkaProducerFactory(getKafkaProps());
    }

    @Bean // Neccessary
    KafkaTemplate<String,String> getKafkaTemplate(){
        return new KafkaTemplate(getProducerFactory());
    }

    @Bean
    ObjectMapper getObjectMapper(){
        return new ObjectMapper();
    }


}
