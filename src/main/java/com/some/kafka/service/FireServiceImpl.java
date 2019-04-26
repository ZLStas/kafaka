package com.some.kafka.service;

import com.google.protobuf.Timestamp;
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
public class FireServiceImpl implements FireService {

    @NonNull
    private final KafkaTemplate<String, com.some.kafka.model.fire.FireEvent> kafkaTemplate;

    @NonNull
    private final Clock clock;

    @Override
    public ListenableFuture<Void> publishEvent(com.some.kafka.model.fire.FireEvent event) {
        SettableListenableFuture<Void> future = new SettableListenableFuture<>();

        // propagate exceptions and discard result as it is not used
        kafkaTemplate
                .sendDefault(event.getId(), event)
                .addCallback(result -> {
                    future.set(null);
                }, future::setException);

        return future;
    }



}
