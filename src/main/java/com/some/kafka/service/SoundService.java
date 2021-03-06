package com.some.kafka.service;

import com.some.kafka.model.dto.SoundUpsertDto;
import org.springframework.util.concurrent.ListenableFuture;

import javax.validation.constraints.NotNull;


public interface SoundService {

    ListenableFuture<Void> upsertSound(@NotNull(message = "Data for upserting mast be present") SoundUpsertDto dto);

}
