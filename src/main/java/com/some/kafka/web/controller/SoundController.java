package com.some.kafka.web.controller;

import com.some.kafka.model.dto.SoundUpsertDto;
import com.some.kafka.service.SoundService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.SettableListenableFuture;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Optional;


@RequiredArgsConstructor
public class SoundController {

    @NonNull
    private final SoundService soundService;


    @ApiOperation("Performs upsert using provided sound measurement payload ")
    @ApiResponses({@ApiResponse(code = 202, message = "Upsert operation was successfully queued for processing", response = void.class)})
    @PutMapping()
    public ListenableFuture<ResponseEntity<Void>> upsertSound(@ApiParam(value = "Id of the client", required = true) @PathVariable(required = false) String controllerId,
                                                              @ApiParam(value = "Sound payload for upsert", required = true) @RequestBody(required = false) SoundUpsertDto sound) {

        SettableListenableFuture<ResponseEntity<Void>> future = new SettableListenableFuture<>();

        // propagate controller ID, and perform sound upsert
        soundService
                .upsertSound(Optional
                        .ofNullable(sound)
                        .map(SoundUpsertDto::toBuilder)
                        .orElseGet(SoundUpsertDto::builder)
                        .controllerId(controllerId)
                        .build())
                .addCallback(result -> {
                    future.set(ResponseEntity.accepted().build());
                }, future::setException);

        return future;
    }

}
