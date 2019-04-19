package com.some.kafaka.service;

import com.some.kafaka.model.dto.SoundUpsertDto;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;

@Service
public class SoundServiceImpl implements SoundService {

    @Override
    public ListenableFuture<Void> upsertSound(SoundUpsertDto dto) {
        return null;
    }

}
