package com.some.kafaka.service;

import com.some.kafaka.model.dto.TemperatureUpsertDto;
import org.springframework.util.concurrent.ListenableFuture;

public interface TemperatureService {

    public ListenableFuture<Void> upsertTemperature(TemperatureUpsertDto dto);

}
