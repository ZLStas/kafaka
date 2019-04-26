package com.some.kafka.cofig;


import lombok.*;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Produced;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.annotation.PostConstruct;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import static com.some.kafka.cofig.GeneratedMessageSerde.serde;


@Configuration
public class KafkaConfig {

    @Bean("topic-temperature")
    public NewTopic temperatureTopic(KafkaTopicsProperties topics) {
        return topics.getTemperature().toNewTopic();
    }

    @Bean("topic-fire")
    public NewTopic fireTopic(KafkaTopicsProperties topics) {
        return topics.getFire().toNewTopic();
    }

    @Bean("producer-factory-temperature")
    public ProducerFactory<String, com.some.kafka.model.temperature.TemperatureEvent> temperatureProducerFactory(KafkaProperties kafka,
                                                                                                                 KafkaTopicsProperties topics) {
        var temperatureTopic = topics.getTemperature();

        var factory = new DefaultKafkaProducerFactory<String, com.some.kafka.model.temperature.TemperatureEvent>(
                temperatureTopic.buildProducerConfig(kafka.buildProducerProperties()));

        factory.setKeySerializer(temperatureTopic.getKeySerializer());
        factory.setValueSerializer(temperatureTopic.getValueSerializer());

        return factory;
    }

    @Bean("producer-factory-fire")
    public ProducerFactory<String, com.some.kafka.model.fire.FireEvent> fireProducerFactory(KafkaProperties kafka,
                                                                                                                 KafkaTopicsProperties topics) {
        var fireTopic = topics.getFire();

        var factory = new DefaultKafkaProducerFactory<String, com.some.kafka.model.fire.FireEvent>(
                fireTopic.buildProducerConfig(kafka.buildProducerProperties()));

        factory.setKeySerializer(fireTopic.getKeySerializer());
        factory.setValueSerializer(fireTopic.getValueSerializer());

        return factory;
    }

    @Bean("template-fire")
    public KafkaTemplate<String, com.some.kafka.model.fire.FireEvent> fireKafkaTemplate(KafkaTopicsProperties topics,
                                                                                                             @Qualifier("producer-factory-fire") ProducerFactory<String, com.some.kafka.model.fire.FireEvent> factory) {
        KafkaTemplate<String, com.some.kafka.model.fire.FireEvent> kafkaTemplate = new KafkaTemplate<>(factory);

        kafkaTemplate.setDefaultTopic(topics.getFire().getName());

        return kafkaTemplate;
    }

    @Bean("template-temperature")
    public KafkaTemplate<String, com.some.kafka.model.temperature.TemperatureEvent> temperatureKafkaTemplate(KafkaTopicsProperties topics,
                                                                                                             @Qualifier("producer-factory-temperature") ProducerFactory<String, com.some.kafka.model.temperature.TemperatureEvent> factory) {
        KafkaTemplate<String, com.some.kafka.model.temperature.TemperatureEvent> kafkaTemplate = new KafkaTemplate<>(factory);

        kafkaTemplate.setDefaultTopic(topics.getTemperature().getName());

        return kafkaTemplate;
    }


    @Data
    @Builder(toBuilder = true)
    @NoArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Validated
    @Component("kafkaTopicsProperties")
    @ConfigurationProperties("kafka.topics")
    public static class KafkaTopicsProperties {

        @Valid
        @NotNull
        private KafkaTopicProperties<String, com.some.kafka.model.temperature.TemperatureEvent> temperature;

        @Valid
        @NotNull
        private KafkaTopicProperties<String, com.some.kafka.model.fire.FireEvent> fire;

        @PostConstruct
        private void init() {
            temperature.setKeySerde(Serdes.String());
            temperature.setValueSerde(serde(com.some.kafka.model.temperature.TemperatureEvent.class));

            fire.setKeySerde(Serdes.String());
            fire.setValueSerde(serde(com.some.kafka.model.fire.FireEvent.class));

        }

        @Data
        @Builder(toBuilder = true)
        @NoArgsConstructor
        @AllArgsConstructor(access = AccessLevel.PRIVATE)
        public static class KafkaTopicProperties<K, V> {

            @NotBlank
            private String name;

            @NotNull
            @Min(1)
            private int partitions = 12;

            @NotNull
            @Min(1)
            private short replication = 3;

            @NotBlank
            private String cleanupPolicy = TopicConfig.CLEANUP_POLICY_DELETE;

            @NotNull
            @Min(-1)
            private Long retentionBytes = -1L;

            @NotNull
            @Min(-1)
            private Long retentionMs = -1L;

            @NotNull
            private KafkaProperties.Consumer consumer = new KafkaProperties.Consumer();

            @NotNull
            private KafkaProperties.Producer producer = new KafkaProperties.Producer();

            private Serde<K> keySerde;

            private Serde<V> valueSerde;

            public Serializer<K> getKeySerializer() {
                return keySerde != null ? keySerde.serializer() : null;
            }

            public Deserializer<K> getKeyDeserializer() {
                return keySerde != null ? keySerde.deserializer() : null;
            }

            public Serializer<V> getValueSerializer() {
                return valueSerde != null ? valueSerde.serializer() : null;
            }

            public Deserializer<V> getValueDeserializer() {
                return valueSerde != null ? valueSerde.deserializer() : null;
            }

            public Consumed<K, V> consumed() {
                return Consumed.with(getKeySerde(), getValueSerde());
            }

            public Produced<K, V> produced() {
                return Produced.with(getKeySerde(), getValueSerde());
            }

            public NewTopic toNewTopic() {
                NewTopic topic = new NewTopic(name, partitions, replication);

                topic.configs(buildTopicConfig());

                return topic;
            }

            public Map<String, String> buildTopicConfig() {
                return buildConfig(Collections.emptyMap(), config -> {
                    config.put(TopicConfig.CLEANUP_POLICY_CONFIG, cleanupPolicy);
                    config.put(TopicConfig.RETENTION_BYTES_CONFIG, retentionBytes.toString());
                    config.put(TopicConfig.RETENTION_MS_CONFIG, retentionMs.toString());
                });
            }

            public Map<String, Object> buildProducerConfig(Map<String, Object> defaults) {
                return buildConfig(defaults, config -> config.putAll(producer.buildProperties()));
            }

            public Map<String, Object> buildConsumerConfig(Map<String, Object> defaults) {
                return buildConfig(defaults, config -> config.putAll(consumer.buildProperties()));
            }

            private <L, R> Map<L, R> buildConfig(Map<L, R> defaults, Consumer<Map<L, R>> overrides) {
                Map<L, R> config = new LinkedHashMap<>();

                Optional.ofNullable(defaults).ifPresent(config::putAll);
                Optional.ofNullable(overrides).ifPresent(o -> o.accept(config));

                return Collections.unmodifiableMap(config);
            }

        }

    }

    @Data
    @Builder(toBuilder = true)
    @NoArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Validated
    @Component("kafkaWorkersProperties")
    @ConfigurationProperties("kafka.workers")
    public static class KafkaWorkersProperties {

        @Valid
        @NotNull
        private KafkaWorkerProperties temperature;

        @Valid
        @NotNull
        private KafkaWorkerProperties fire;

        @Data
        public static class KafkaWorkerProperties {

            /**
             * Unique ID for this Kafka Streams application
             */
            @NotBlank
            private String applicationId;

            /**
             * The replication factor for change log topics and repartition topics created by the stream processing application.
             */
            @Min(1)
            private int replicationFactor = 1;

            /**
             * Directory location for state store.
             */
            @NotBlank
            private String stateDir = "/tmp/kafka-streams";

            /**
             * Number of stream threads that are concurrently running stream tasks
             */
            @Min(1)
            private int streamThreads = 1;

            /**
             * Number of milliseconds to wait for Kafka Streams instance to shut down. Zero timeout means wait forever.
             */
            @Min(0)
            private int closeTimeoutMs = 60000;

            /**
             * Whether worker needs to start in debug mode
             */
            private boolean debug = false;

        }

    }

}
