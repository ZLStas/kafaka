package com.some.kafka.service;

import com.some.kafka.model.dto.TemperatureUpsertDto;
import com.some.kafka.model.models.Temperature;
import org.springframework.util.concurrent.ListenableFuture;

public interface TemperatureService {

     ListenableFuture<Void> upsertTemperature(TemperatureUpsertDto dto);

     void testUpsertTemperature(TemperatureUpsertDto dto);

      Temperature getTemperature(String id);

}
