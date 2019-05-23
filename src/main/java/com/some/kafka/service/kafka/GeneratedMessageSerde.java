package com.some.kafka.service.kafka;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.streams.kstream.Joined;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Serialized;
import org.apache.kafka.streams.processor.StateStore;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.protobuf.GeneratedMessageV3;

import lombok.NonNull;

/**
 * Serde that can be used to serialize and deserialize instances of protobuf generated
 * classes.
 */
public class GeneratedMessageSerde<T extends GeneratedMessageV3> implements Serde<T> {

    private static final LoadingCache<Class<? extends GeneratedMessageV3>, Serde<? extends GeneratedMessageV3>> SERDES = CacheBuilder
        .newBuilder()
        .build(new CacheLoader<>() {
            public Serde<? extends GeneratedMessageV3> load(@NonNull Class<? extends GeneratedMessageV3> clazz) {
                return new GeneratedMessageSerde<>(clazz);
            }
        });

    private final Class<T> clazz;

    private final Serializer<T> serializer;
    private final Deserializer<T> deserializer;

    private GeneratedMessageSerde(@NonNull Class<T> clazz) {
        this.clazz = clazz;

        this.serializer = new GeneratedMessageSerializer();
        this.deserializer = new GeneratedMessageDeserializer();
    }

    @SuppressWarnings("unchecked")
    public static <T extends GeneratedMessageV3> Serde<T> serde(Class<T> clazz) {
        return (Serde<T>) SERDES.getUnchecked(clazz);
    }

    public static <K extends GeneratedMessageV3, V extends GeneratedMessageV3> Serialized<K, V> serialized(Class<K> keyClass,
                                                                                                           Class<V> valueClass) {
        return Serialized.with(serde(keyClass), serde(valueClass));
    }

    public static <K extends GeneratedMessageV3, V extends GeneratedMessageV3, S extends StateStore> Materialized<K, V, S> materialized(
        Class<K> keyClass,
        Class<V> valueClass) {
        return Materialized.with(serde(keyClass), serde(valueClass));
    }

    public static <K extends GeneratedMessageV3, V1 extends GeneratedMessageV3, V2 extends GeneratedMessageV3> Joined<K, V1, V2> joined(
        Class<K> keyClass,
        Class<V1> firstValueClass,
        Class<V2> secondValueClass) {
        return Joined.with(serde(keyClass), serde(firstValueClass), serde(secondValueClass));
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        // nothing to do
    }

    @Override
    public void close() {
        // nothing to do
    }

    @Override
    public Serializer<T> serializer() {
        return this.serializer;
    }

    @Override
    public Deserializer<T> deserializer() {
        return this.deserializer;
    }

    private class GeneratedMessageSerializer implements Serializer<T> {

        @Override
        public void configure(Map<String, ?> configs, boolean isKey) {
            // nothing to do
        }

        @Override
        public byte[] serialize(String topic, T data) {
            return data != null ? data.toByteArray() : null;
        }

        @Override
        public void close() {
            // nothing to do
        }

    }

    private class GeneratedMessageDeserializer implements Deserializer<T> {

        private final Method parseFrom;

        private GeneratedMessageDeserializer() {
            try {
                this.parseFrom = clazz.getMethod("parseFrom", byte[].class);
            } catch (NoSuchMethodException ex) {
                throw new RuntimeException("Could not construct deserializer. Parse from method does not exist.", ex);
            }
        }

        @Override
        public void configure(Map<String, ?> configs, boolean isKey) {
            // nothing to do
        }

        @Override
        public T deserialize(String topic, byte[] data) {
            if (data != null) {
                try {
                    Object parsed = parseFrom.invoke(null, (Object) data);

                    if (clazz.isInstance(parsed)) {
                        return clazz.cast(parsed);
                    } else {
                        throw new RuntimeException(
                            "Type parsed using Deserializer is " + parsed.getClass().getName() + " but Deserializer was expecting " +
                                clazz.getName());
                    }
                } catch (IllegalAccessException | InvocationTargetException ex) {
                    throw new RuntimeException(ex);
                }
            }

            return null;
        }

        @Override
        public void close() {
            // nothing to do
        }

    }

}
