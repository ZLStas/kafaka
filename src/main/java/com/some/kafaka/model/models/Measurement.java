package com.some.kafaka.model.models;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.Instant;

public abstract class Measurement {

    @NotBlank(message = "Measurement ID must be present")
    private String id;

    @Valid
    @NotNull(message = "Microcontroller that created this measurement must be present")
    private Microcontroller createdBy;

    @NotNull(message = "Timestamp when this measurement was taken must be present")
    private Instant createdAt;

    @Valid
    private Microcontroller editedBy;

    private Instant editedAt;

}
