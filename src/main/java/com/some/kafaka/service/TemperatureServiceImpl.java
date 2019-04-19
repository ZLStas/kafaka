package com.some.kafaka.service;


import com.google.protobuf.Timestamp;
import com.some.kafaka.model.dto.TemperatureUpsertDto;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.SettableListenableFuture;

import java.time.Clock;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class TemperatureServiceImpl implements TemperatureService {

    @NonNull
    private final KafkaTemplate<String, com.some.kafaka.model.temperature.TemperatureEvent> kafkaTemplate;

    @NonNull
    private final Clock clock;

    @Override
    public ListenableFuture<Void> upsertTemperature(TemperatureUpsertDto dto) {
        return publishEvent(createUpsertEvent(dto));
    }

    public ListenableFuture<Void> createTemperature(TemperatureUpsertDto dto) {
        return null;
    }

    private ListenableFuture<Void> publishEvent(com.some.kafaka.model.temperature.TemperatureEvent event) {
        SettableListenableFuture<Void> future = new SettableListenableFuture<>();

        // propagate exceptions and discard result as it is not used
        kafkaTemplate
                .sendDefault(event.getId(), event)
                .addCallback(result -> {
                    future.set(null);
                }, future::setException);

        return future;
    }

    private com.some.kafaka.model.temperature.TemperatureEvent createUpsertEvent(TemperatureUpsertDto dto) {
        var upserted = com.some.kafaka.model.temperature.TemperatureUpserted.newBuilder().
                setTemperature(com.some.kafaka.model.temperature.TemperatureData.newBuilder().setValue(dto.getValue()).build())
                .build();

        var event = com.some.kafaka.model.temperature.TemperatureEvent.
                newBuilder().
                setCreatedBy(dto.getControllerId()).
                setCreatedAt(timestamp()).setId(dto.getId()).setTemperatureUpserted(upserted).build();

        return event;
    }

    private Timestamp timestamp() {
        Instant now = clock.instant();
        return Timestamp.newBuilder().setSeconds(now.getEpochSecond()).setNanos(now.getNano()).build();
    }

}
