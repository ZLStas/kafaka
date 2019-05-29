package com.some.kafka.service;


import com.google.protobuf.Timestamp;
import com.some.kafka.dao.TemperatureDao;
import com.some.kafka.model.dto.TemperatureUpsertDto;
import com.some.kafka.model.models.Temperature;
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
    private final KafkaTemplate<String, com.some.kafka.model.temperature.TemperatureEvent> kafkaTemplate;

    @NonNull
    private final Clock clock;

    @NonNull
    private TemperatureDao temperatureDao;

    @Override
    public ListenableFuture<Void> upsertTemperature(TemperatureUpsertDto dto) {
        return publishEvent(createUpsertEvent(dto));
    }

    @Override
    public Temperature getTemperature(String id) {
        return temperatureDao.getTemperature(id);
    }

    @Override
    public void testUpsertTemperature(TemperatureUpsertDto dto) {
        System.out.println(dto.toString());
    }

    public ListenableFuture<Void> createTemperature(TemperatureUpsertDto dto) {
        return null;
    }

    private ListenableFuture<Void> publishEvent(com.some.kafka.model.temperature.TemperatureEvent event) {
        SettableListenableFuture<Void> future = new SettableListenableFuture<>();

        // propagate exceptions and discard result as it is not used
        kafkaTemplate
            .sendDefault(event.getId(), event)
            .addCallback(result -> {
                future.set(null);
            }, future::setException);

        return future;
    }

    private com.some.kafka.model.temperature.TemperatureEvent createUpsertEvent(TemperatureUpsertDto dto) {
        var upserted = com.some.kafka.model.temperature.TemperatureUpserted.newBuilder().
            setTemperature(com.some.kafka.model.temperature.TemperatureData.newBuilder().setValue(dto.getValue()).build())
            .build();

        var event = com.some.kafka.model.temperature.TemperatureEvent.
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
