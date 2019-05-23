package com.some.kafka.service.kafka;

import com.some.kafka.cofig.KafkaConfig.KafkaWorkersProperties.KafkaWorkerProperties;
import com.some.kafka.model.fire.FireEvent;
import com.some.kafka.model.temperature.TemperatureEvent;
import com.some.kafka.service.FireService;
import com.some.kafka.service.TemperatureService;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.Clock;

import static com.some.kafka.cofig.KafkaConfig.KafkaTopicsProperties.KafkaTopicProperties;


@Slf4j
@Service
@ConditionalOnProperty(name = "kafka.workers.temperature.enabled", matchIfMissing = true)
public class TemperatureWorker extends KafkaWorker {

    private final KafkaTopicProperties<String, com.some.kafka.model.temperature.TemperatureEvent> temperatureTopic;
    private final KafkaTopicProperties<String, com.some.kafka.model.fire.FireEvent> fireTopic;
    private final TemperatureService temperatureService;
    private final FireService fireService;
    private final Clock clock;
    private Serde<FireEvent> fireEventSerde;
    private Serde<String> stringSerde;

    @Autowired
    public TemperatureWorker(@NonNull KafkaProperties kafkaProperties,
                             @Value("#{kafkaWorkersProperties.temperature}") KafkaWorkerProperties workerProperties,
                             @Value("#{kafkaTopicsProperties.temperature}") @NonNull KafkaTopicProperties<String, com.some.kafka.model.temperature.TemperatureEvent> temperatureTopic,
                             @Value("#{kafkaTopicsProperties.fire}") @NonNull KafkaTopicProperties<String, com.some.kafka.model.fire.FireEvent> fireTopic,
                             TemperatureService temperatureService, FireService fireService, @NonNull Clock clock) {
        super(kafkaProperties, workerProperties);
        this.temperatureTopic = temperatureTopic;
        this.fireTopic = fireTopic;
        this.temperatureService = temperatureService;
        this.fireService = fireService;
        this.clock = clock;
    }

    @PostConstruct
    private void initTemperatureWorker() {
        this.fireEventSerde = fireTopic.getValueSerde();
        this.stringSerde = Serdes.String();
    }

    @Override
    protected void createTopology(@NonNull StreamsBuilder builder) {
        var temperatureStream = temperatureStream(builder);
        fireTable(temperatureStream).toStream().foreach((key, value) -> System.out.println(value));
    }

    private KStream<String, com.some.kafka.model.temperature.TemperatureEvent> temperatureStream(StreamsBuilder builder) {
        return builder.stream(this.temperatureTopic.getName(), this.temperatureTopic.consumed());
    }

    private KTable<String, FireEvent> fireTable(KStream<String, TemperatureEvent> temperatureStream) {
        return temperatureStream.groupByKey()
            .aggregate(FireEvent::getDefaultInstance, this::updateFireState, Materialized.as("fire-store").with(stringSerde, fireEventSerde));
    }

    private FireEvent updateFireState(String id, TemperatureEvent newTemperatureEvent, FireEvent aggregated) {
        Integer newTemperature = newTemperatureEvent.getTemperatureUpserted().getTemperature().getValue();

        if (aggregated.hasFireStarted() && newTemperature <= 55) {
            return temperatureToFireStopped(newTemperatureEvent);
        }
        if (newTemperature > 55) {
            return temperatureToFireStarted(newTemperatureEvent);
        }
        return temperatureToFireWarning(newTemperatureEvent);
    }

    public com.some.kafka.model.fire.FireEvent temperatureToFireStarted(com.some.kafka.model.temperature.TemperatureEvent temperatureEvent) {

        com.some.kafka.model.fire.FireStarted fireStarted = com.some.kafka.model.fire.FireStarted.newBuilder().build();
        var event = com.some.kafka.model.fire.FireEvent.
            newBuilder().
            setCreatedBy(temperatureEvent.getCreatedBy()).
            setCreatedAt(temperatureEvent.getCreatedAt()).setId(temperatureEvent.getId()).setFireStarted(fireStarted).build();

        return event;
    }

    public com.some.kafka.model.fire.FireEvent temperatureToFireWarning(com.some.kafka.model.temperature.TemperatureEvent temperatureEvent) {

        com.some.kafka.model.fire.FireWarning fireWarning = com.some.kafka.model.fire.FireWarning.newBuilder().build();

        var event = com.some.kafka.model.fire.FireEvent.
            newBuilder().
            setCreatedBy(temperatureEvent.getCreatedBy()).
            setCreatedAt(temperatureEvent.getCreatedAt()).setId(temperatureEvent.getId()).setFireWarning(fireWarning).build();

        return event;
    }

    public com.some.kafka.model.fire.FireEvent temperatureToFireStopped(com.some.kafka.model.temperature.TemperatureEvent temperatureEvent) {

        com.some.kafka.model.fire.FireStopped fireStopped = com.some.kafka.model.fire.FireStopped.newBuilder().build();

        var event = com.some.kafka.model.fire.FireEvent.
            newBuilder().
            setCreatedBy(temperatureEvent.getCreatedBy()).
            setCreatedAt(temperatureEvent.getCreatedAt()).setId(temperatureEvent.getId()).setFireStopped(fireStopped).build();

        return event;
    }

}
