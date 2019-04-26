package com.some.kafka.service.kafka;

import com.google.protobuf.Timestamp;
import com.some.kafka.cofig.KafkaConfig.KafkaWorkersProperties.KafkaWorkerProperties;
import com.some.kafka.service.FireService;
import com.some.kafka.service.TemperatureService;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.StreamsBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;

import static com.some.kafka.cofig.KafkaConfig.KafkaTopicsProperties.KafkaTopicProperties;

@Slf4j
@Service
@ConditionalOnProperty(name = "kafka.workers.temperature.enabled", matchIfMissing = true)
public class FireWorker extends KafkaWorker {

    private final KafkaTopicProperties<String, com.some.kafka.model.fire.FireEvent> fireTopic;
    private final FireService fireService;
    private final Clock clock;

    @Autowired
    public FireWorker(@NonNull KafkaProperties kafkaProperties,
                      @Value("#{kafkaWorkersProperties.fire}") KafkaWorkerProperties workerProperties,
                      @Value("#{kafkaTopicsProperties.fire}") @NonNull KafkaTopicProperties<String, com.some.kafka.model.fire.FireEvent> fireTopic,
                      TemperatureService temperatureService, FireService fireService, @NonNull Clock clock) {
        super(kafkaProperties, workerProperties);
        this.fireTopic = fireTopic;
        this.fireService = fireService;
        this.clock = clock;
    }

    @Override
    protected void createTopology(@NonNull StreamsBuilder builder) {
        fireStream(builder);
    }

    private void fireStream(StreamsBuilder builder) {
        builder.stream(fireTopic.getName(), fireTopic.consumed()).foreach((key, value) -> System.out.println(key + value));

    }


    public com.some.kafka.model.fire.FireEvent temperatureToFireWarning(com.some.kafka.model.temperature.TemperatureEvent temperatureEvent) {

        com.some.kafka.model.fire.FireWarning fireWarning = com.some.kafka.model.fire.FireWarning.newBuilder().build();

        var event = com.some.kafka.model.fire.FireEvent.
                newBuilder().
                setCreatedBy(temperatureEvent.getCreatedBy()).
                setCreatedAt(timestamp()).setId(temperatureEvent.getId()).setFireWarning(fireWarning).build();

        return event;
    }

    public com.some.kafka.model.fire.FireEvent temperatureToFireStopped(com.some.kafka.model.temperature.TemperatureEvent temperatureEvent) {

        com.some.kafka.model.fire.FireStopped fireStopped = com.some.kafka.model.fire.FireStopped.newBuilder().build();

        var event = com.some.kafka.model.fire.FireEvent.
                newBuilder().
                setCreatedBy(temperatureEvent.getCreatedBy()).
                setCreatedAt(timestamp()).setId(temperatureEvent.getId()).setFireStopped(fireStopped).build();

        return event;
    }

    private Timestamp timestamp() {
        Instant now = clock.instant();
        return Timestamp.newBuilder().setSeconds(now.getEpochSecond()).setNanos(now.getNano()).build();
    }
}
