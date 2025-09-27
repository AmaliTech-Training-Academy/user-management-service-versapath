package com.capstone.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.common.event.ProduceUserEvent;
import org.common.event.ProduceMentorEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaProducer {
    private final KafkaTemplate<String, ProduceUserEvent> kafkaTemplate;
    private final KafkaTemplate<String, ProduceMentorEvent> mentorKafkaTemplate;

    @Value("${KAFKA_USER_TOPIC:user.create}")
    private String userTopic;

    @Value("${KAFKA_MENTOR_TOPIC:mentor.create}")
    private String mentorTopic;

    @Value("${KAFKA_USER_UPDATE_TOPIC:user.update}")
    private String userUpdateTopic;

    @Value("${KAFKA_MENTOR_UPDATE_TOPIC:mentor.update}")
    private String mentorUpdateTopic;

    public void produce(ProduceUserEvent event) {
        log.info("Sending event to Kafka topic: {}", "user.create");
        kafkaTemplate.send(userTopic, event);
        log.info("user event is populated: {}", event);
    }

    public void produceMentor(ProduceMentorEvent event) {
        log.info("Sending mentor event to Kafka topic: {}", mentorTopic);
        mentorKafkaTemplate.send(mentorTopic, event);
        log.info("Mentor event is populated: {}", event);
    }

    public void produceUserUpdate(ProduceUserEvent event) {
        log.info("Sending user update event to Kafka topic: {}", userUpdateTopic);
        kafkaTemplate.send(userUpdateTopic, event);
        log.info("User update event is populated: {}", event);
    }

    public void produceMentorUpdate(ProduceMentorEvent event) {
        log.info("Sending mentor update event to Kafka topic: {}", mentorUpdateTopic);
        mentorKafkaTemplate.send(mentorUpdateTopic, event);
        log.info("Mentor update event is populated: {}", event);
    }
}
