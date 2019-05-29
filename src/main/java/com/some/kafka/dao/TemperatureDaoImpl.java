package com.some.kafka.dao;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.some.kafka.model.models.Temperature;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TemperatureDaoImpl implements TemperatureDao {
    private static final String TEMPERATURE_TABLE_NAME = "Temperature";
    private static final String TEMPERATURE_ID = "Id";
    private static final String TEMPERATURE_VALUE = "Value";

    private static final String TEMPERATURE_CREATED_AT = "CreatedAt";
    private static final String TEMPERATURE_EDITED_ET = "EditedAt";
    private static final String TEMPERATURE_CREATED_BY = "CreatedBy";
    private static final String TEMPERATURE_EDITED_BY = "EditedBy";

    @NonNull
    private AmazonDynamoDB client;

    @NonNull
    private ObjectMapper mapper;

    @Override
    public void saveTemperature(Temperature temperature) {
        HashMap<String, AttributeValue> item_values =
            new HashMap<String, AttributeValue>();
        item_values.put(TEMPERATURE_ID, new AttributeValue(temperature.getId()));
        item_values.put(TEMPERATURE_CREATED_AT, new AttributeValue(String.valueOf(temperature.getCreatedAt())));
        item_values.put(TEMPERATURE_CREATED_BY, new AttributeValue(temperature.getCreatedBy()));
        item_values.put(TEMPERATURE_VALUE, new AttributeValue(temperature.getValue().toString()));

        if (Optional.ofNullable(temperature.getEditedAt()).isPresent()) {
            item_values.put(TEMPERATURE_EDITED_ET, new AttributeValue(String.valueOf(temperature.getEditedAt())));
            item_values.put(TEMPERATURE_EDITED_BY, new AttributeValue(temperature.getEditedBy()));
        }
        client.putItem(TEMPERATURE_TABLE_NAME, item_values);
    }

    @Override
    public Temperature getTemperature(String id) {

        GetItemSpec spec = new GetItemSpec().withPrimaryKey(TEMPERATURE_ID, id);
        DynamoDB dynamoDB = new DynamoDB(client);
        Table table = dynamoDB.getTable(TEMPERATURE_TABLE_NAME);
        Temperature temperature = null;
        Item outcome = null;
        try {
            System.out.printf("Attempting to read the ${id} item...", id);
            outcome = table.getItem(spec);
            System.out.println("Get Item succeeded: " + outcome);

        } catch (Exception e) {
            System.err.println("Unable to read item: " + id);
            System.err.println(e.getMessage());
        }
        if (Optional.ofNullable(outcome).isPresent()) {
            temperature = itemToTemperature(outcome);
        }
        System.out.println(temperature);
        return temperature;
    }

    private Temperature itemToTemperature(Item item) {
        return mapper.convertValue(item,Temperature.class);
        var builder = Temperature.builder()
            .id(item.getString(TEMPERATURE_ID))
            .value(item.getInt(TEMPERATURE_VALUE))
            .createdAt(item.getLong(TEMPERATURE_CREATED_AT))
            .createdBy(item.getString(TEMPERATURE_CREATED_BY));
        if (item.isNull(TEMPERATURE_EDITED_ET) && item.isNull(TEMPERATURE_EDITED_BY)) {
            return builder.editedAt(item.getLong(TEMPERATURE_EDITED_ET))
                .editedBy(item.getString(TEMPERATURE_EDITED_BY)).build();
        }
        return builder.build();
    }
}