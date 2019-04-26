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
import java.util.Optional;

import static com.some.kafka.cofig.KafkaConfig.KafkaTopicsProperties.KafkaTopicProperties;

@Slf4j
@Service
@ConditionalOnProperty(name = "kafka.workers.temperature.enabled", matchIfMissing = true)
public class TemperatureWorker extends KafkaWorker {

    private final KafkaTopicProperties<String, com.some.kafka.model.temperature.TemperatureEvent> temperatureTopic;
    private final TemperatureService temperatureService;
    private final FireService fireService;
    private final Clock clock;

    @Autowired
    public TemperatureWorker(@NonNull KafkaProperties kafkaProperties,
                             @Value("#{kafkaWorkersProperties.temperature}") KafkaWorkerProperties workerProperties,
                             @Value("#{kafkaTopicsProperties.temperature}") @NonNull KafkaTopicProperties<String, com.some.kafka.model.temperature.TemperatureEvent> temperatureTopic,
                             TemperatureService temperatureService, FireService fireService, @NonNull Clock clock) {
        super(kafkaProperties, workerProperties);
        this.temperatureTopic = temperatureTopic;
        this.temperatureService = temperatureService;
        this.fireService = fireService;
        this.clock = clock;
    }

    @Override
    protected void createTopology(@NonNull StreamsBuilder builder) {
        temperatureStream(builder);
    }

    private void temperatureStream(StreamsBuilder builder) {
        var topic = builder.stream(temperatureTopic.getName(), temperatureTopic.consumed());
        topic.peek((key, value) -> checkFire(value)).foreach(((key, value) -> System.out.println(key + value)));
    }


    private void checkFire(com.some.kafka.model.temperature.TemperatureEvent event) {
        switch (temperature){

        }
        if (getPayload(event).isPresent() && getPayload(event).get() > 55) {
            fireService.publishEvent(temperatureToFireStarted(event));
        }
    }

    private Optional<Integer> getPayload(com.some.kafka.model.temperature.TemperatureEvent event) {
        return Optional.ofNullable(event.getTemperatureUpserted().getTemperature().getValue());
    }

    public com.some.kafka.model.fire.FireEvent temperatureToFireStarted(com.some.kafka.model.temperature.TemperatureEvent temperatureEvent) {

        com.some.kafka.model.fire.FireStarted fireStarted = com.some.kafka.model.fire.FireStarted.newBuilder().build();
        var event = com.some.kafka.model.fire.FireEvent.
                newBuilder().
                setCreatedBy(temperatureEvent.getCreatedBy()).
                setCreatedAt(timestamp()).setId(temperatureEvent.getId()).setFireStarted(fireStarted).build();

        return event;
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
