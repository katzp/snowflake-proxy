package com.pkatz.snowflake.proxy.config;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import org.socialsignin.spring.data.dynamodb.repository.config.EnableDynamoDBRepositories;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@EnableDynamoDBRepositories(basePackages = {"com.pkatz.snowflake.proxy.repository"})
public class DynamoDBConfig {
    @Bean
    @Profile("local")
    public AmazonDynamoDB amazonDynamoDB(
            @Value("${amazon.dynamodb.endpoint}") final String amazonDynamoDBEndpoint
    ) {
        AmazonDynamoDBClientBuilder dynamoBuilder = AmazonDynamoDBClientBuilder.standard();
        dynamoBuilder.withEndpointConfiguration(
                new AwsClientBuilder.EndpointConfiguration(amazonDynamoDBEndpoint, null)
        );
        return dynamoBuilder.build();
    }
}
