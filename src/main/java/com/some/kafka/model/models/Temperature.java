package com.some.kafka.model.models;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.Instant;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@DynamoDBTable(tableName = "Temperature")
public class Temperature {

    @NotBlank(message = "Measurement ID must be present")
    @DynamoDBHashKey(attributeName = "Id")
    private String id;

    @Valid
    @NotNull(message = "Microcontroller that created this measurement must be present")
    @DynamoDBAttribute(attributeName = "CreatedBy")
    private String createdBy;

    @Valid
    @DynamoDBAttribute(attributeName = "EditedBy")
    private String editedBy;

    @NotNull(message = "Timestamp when this measurement was taken must be present")
    @DynamoDBAttribute(attributeName = "CreatedAt")
    private Long createdAt;

    @DynamoDBAttribute(attributeName = "EditedAt")
    private Long editedAt;

    @DynamoDBAttribute(attributeName = "Value")
    Integer value;

}
