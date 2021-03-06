package com.some.kafka.dao;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.some.kafka.model.models.Temperature;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TemperatureDaoImpl implements TemperatureDao {

    @NonNull
    private AmazonDynamoDB client;

    @Override
    public void saveTemperature(Temperature temperature) {
        DynamoDBMapper mapper = new DynamoDBMapper(client);
        mapper.save(temperature);
    }

    @Override
    public Temperature getTemperature(String id) {
        DynamoDBMapper mapper = new DynamoDBMapper(client);
        return mapper.load(Temperature.class, id);

    }
}