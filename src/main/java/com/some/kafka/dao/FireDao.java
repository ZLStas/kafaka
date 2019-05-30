package com.some.kafka.dao;

import com.some.kafka.model.models.Fire;
import com.some.kafka.model.models.Temperature;

public interface FireDao {

    void saveFire(Fire fire);

}
