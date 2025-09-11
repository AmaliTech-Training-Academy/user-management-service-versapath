package com.capstone.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.common.event.ProduceUserEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaProducer {
    private final KafkaTemplate<String, ProduceUserEvent> kafkaTemplate;

    public void produce(ProduceUserEvent event) {
        log.info("Sending event to Kafka topic: {}", "user.create");
        kafkaTemplate.send("user.create", event);
        log.info("user event is populated: {}", event);
    }

}
