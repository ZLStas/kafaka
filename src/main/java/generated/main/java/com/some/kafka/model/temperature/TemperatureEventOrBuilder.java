// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: TemperatureEvents.proto

package com.some.kafka.model.temperature;

public interface TemperatureEventOrBuilder extends
    // @@protoc_insertion_point(interface_extends:TemperatureEvent)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>string id = 1;</code>
   */
  java.lang.String getId();
  /**
   * <code>string id = 1;</code>
   */
  com.google.protobuf.ByteString
      getIdBytes();

  /**
   * <code>string created_by = 2;</code>
   */
  java.lang.String getCreatedBy();
  /**
   * <code>string created_by = 2;</code>
   */
  com.google.protobuf.ByteString
      getCreatedByBytes();

  /**
   * <code>.google.protobuf.Timestamp created_at = 3;</code>
   */
  boolean hasCreatedAt();
  /**
   * <code>.google.protobuf.Timestamp created_at = 3;</code>
   */
  com.google.protobuf.Timestamp getCreatedAt();
  /**
   * <code>.google.protobuf.Timestamp created_at = 3;</code>
   */
  com.google.protobuf.TimestampOrBuilder getCreatedAtOrBuilder();

  /**
   * <code>.TemperatureUpserted temperature_upserted = 4;</code>
   */
  boolean hasTemperatureUpserted();
  /**
   * <code>.TemperatureUpserted temperature_upserted = 4;</code>
   */
  com.some.kafka.model.temperature.TemperatureUpserted getTemperatureUpserted();
  /**
   * <code>.TemperatureUpserted temperature_upserted = 4;</code>
   */
  com.some.kafka.model.temperature.TemperatureUpsertedOrBuilder getTemperatureUpsertedOrBuilder();

  public com.some.kafka.model.temperature.TemperatureEvent.EventCase getEventCase();
}
