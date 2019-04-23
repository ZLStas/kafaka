package com.some.kafka.service;

import com.some.kafka.model.dto.SoundUpsertDto;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;

@Service
public class SoundServiceImpl implements SoundService {

    @Override
    public ListenableFuture<Void> upsertSound(SoundUpsertDto dto) {
        return null;
    }

}
