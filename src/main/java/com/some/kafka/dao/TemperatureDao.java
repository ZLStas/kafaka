package com.some.kafka.dao;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.some.kafka.model.models.Temperature;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

public interface TemperatureDao {

    void saveTemperature(Temperature temperature);

    Temperature getTemperature(String id);

}
