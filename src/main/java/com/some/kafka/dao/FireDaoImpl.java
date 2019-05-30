package com.some.kafka.dao;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.some.kafka.model.models.Fire;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class FireDaoImpl implements FireDao {

    @NonNull
    private AmazonDynamoDB client;

    @Override
    public void saveFire(Fire fire) {
        DynamoDBMapper mapper = new DynamoDBMapper(client);
        mapper.save(fire);
    }

}