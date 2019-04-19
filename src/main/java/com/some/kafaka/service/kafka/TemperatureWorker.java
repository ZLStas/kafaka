package com.some.kafaka.service.kafka;//package com.diplome.demo.service.kafka;
//
//import com.diplome.demo.config.KafkaConfig;
//import com.diplome.demo.model.temperature.TemperatureEvent;
//import com.diplome.demo.service.TemperatureService;
//import lombok.NonNull;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.kafka.streams.StreamsBuilder;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
//import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
//import org.springframework.stereotype.Service;
//
//import static com.diplome.demo.config.KafkaConfig.KafkaTopicsProperties.KafkaTopicProperties;
//
//@Slf4j
//@Service
//@ConditionalOnProperty(name = "kafka.workers.temperature.enabled", matchIfMissing = true)
//public class TemperatureWorker extends KafkaWorker {
//
//    private final StreamsBuilder builder;
//    private final KafkaTopicProperties<String, com.diplome.demo.model.temperature.TemperatureEvent> temperatureTopic;
//
//    private final TemperatureService temperatureService;
//
//    @Autowired
//    public TemperatureWorker(@NonNull KafkaProperties kafkaProperties,
//                             @Value("#{kafkaWorkersProperties.temperature}") KafkaConfig.KafkaWorkersProperties.@NonNull KafkaWorkerProperties workerProperties,
//                             StreamsBuilder builder, KafkaTopicProperties<String, TemperatureEvent> temperatureTopic,
//                             TemperatureService temperatureService) {
//        super(kafkaProperties, workerProperties);
//        this.builder = builder;
//        this.temperatureTopic = temperatureTopic;
//        this.temperatureService = temperatureService;
//    }
//
//    @Override
//    protected void createTopology(@NonNull StreamsBuilder builder) {
//        temperatureStream(builder);
//    }
//
//    private void temperatureStream(StreamsBuilder builder) {
//        builder.stream(temperatureTopic.getName(), temperatureTopic.consumed());
//    }
//
//}
