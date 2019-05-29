package com.some.kafka.model.models;

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
public class Temperature {

    @NotBlank(message = "Measurement ID must be present")
    private String id;

    @Valid
    @NotNull(message = "Microcontroller that created this measurement must be present")
    private String createdBy;

    @Valid
    private String editedBy;

    @NotNull(message = "Timestamp when this measurement was taken must be present")
    private Long createdAt;

    private Long editedAt;

    Integer value;

}
