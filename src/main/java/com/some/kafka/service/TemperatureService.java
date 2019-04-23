package com.some.kafka.service;

import com.some.kafka.model.dto.TemperatureUpsertDto;
import org.springframework.util.concurrent.ListenableFuture;

public interface TemperatureService {

    public ListenableFuture<Void> upsertTemperature(TemperatureUpsertDto dto);

    public void testUpsertTemperature(TemperatureUpsertDto dto);

}
