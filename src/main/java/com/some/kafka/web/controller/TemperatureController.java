package com.some.kafka.web.controller;

import com.some.kafka.model.dto.TemperatureUpsertDto;
import com.some.kafka.service.TemperatureService;
import io.swagger.annotations.*;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.SettableListenableFuture;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Api(tags = {"Temperature information"})
@Slf4j
@CrossOrigin
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "home-measurement-analyzation/{controllerId}/temperature", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
//TODO investigate which payload mast be here...
public class TemperatureController {

    @NonNull
    private final TemperatureService temperatureService;

    @ApiOperation("Performs upsert using provided temperature measurement payload ")
    @ApiResponses({@ApiResponse(code = 202, message = "Upsert operation was successfully queued for processing", response = void.class)})
    @PutMapping()
    public ListenableFuture<ResponseEntity<Void>> upsertTemperature(@ApiParam(value = "Id of the controller", required = true) @PathVariable(required = false) String controllerId,
                                                                    @ApiParam(value = "Temperature payload for upsert", required = true) @RequestBody(required = false) TemperatureUpsertDto temperature) {

        SettableListenableFuture<ResponseEntity<Void>> future = new SettableListenableFuture<>();

         //propagate controller ID, and perform sound upsert
        temperatureService
                .upsertTemperature(Optional
                        .ofNullable(temperature)
                        .map(TemperatureUpsertDto::toBuilder)
                        .orElseGet(TemperatureUpsertDto::builder)
                        .controllerId(controllerId)
                        .build())
                .addCallback(result -> {
                    future.set(ResponseEntity.accepted().build());
                }, future::setException);

        return future;
    }

}
