package com.some.kafka.cofig;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AwsConfig {

    @Bean
    public AmazonDynamoDB dynamoDB(@Value("${storage.dynamoDB.region}") String region, AWSCredentialsProvider credentials) {
        return  AmazonDynamoDBClientBuilder.standard()
            .withRegion(region)
            .withCredentials(credentials)
            .build();
    }

    @Bean
    public AWSCredentialsProvider awsCredentials(@Value("${storage.dynamoDB.key}") String key, @Value("${storage.dynamoDB.secret}") String secret) {
        return new AWSStaticCredentialsProvider(new BasicAWSCredentials(key, secret));
    }

}
