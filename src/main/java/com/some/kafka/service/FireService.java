package com.some.kafka.service;

import org.springframework.util.concurrent.ListenableFuture;

import javax.validation.constraints.NotNull;

public interface FireService {

     ListenableFuture<Void> publishEvent( @NotNull com.some.kafka.model.fire.FireEvent event);

}
