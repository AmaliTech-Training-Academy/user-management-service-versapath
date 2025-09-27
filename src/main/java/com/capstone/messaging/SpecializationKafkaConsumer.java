package com.capstone.messaging;

import com.capstone.service.SpecializationService;
import com.capstone.exception.SpecializationProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.common.event.TalentRouteEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class SpecializationKafkaConsumer {

    private final SpecializationService specializationService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${KAFKA_TALENT_ROUTE_DLT_TOPIC:talentRoute.create.dlt}")
    private String talentRouteDltTopic;

    @KafkaListener(topics = "${KAFKA_TALENT_ROUTE_CREATE_TOPIC:talentRoute.create}")
    @Retryable(
            retryFor = {SpecializationProcessingException.class, Exception.class},
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void listenSpecializationCreate(
            @Payload TalentRouteEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        log.info("Received specialization.create event from topic: {}, partition: {}, offset: {}, specId: {}",
                topic, partition, offset, event.getId());

        try {
            // Validate event
            validateSpecializationEvent(event);

            // Process the specialization create event
            specializationService.createSpecialization(event);

            log.info("Successfully processed specialization create event for specId: {}",
                    event.getId());

            // Acknowledge message only after successful processing
            acknowledgment.acknowledge();

        } catch (SpecializationProcessingException e) {
            log.error("Failed to process specialization create event for specId: {}. Error: {}",
                    event.getId(), e.getMessage(), e);
            handleProcessingFailure(event, acknowledgment);

        } catch (Exception e) {
            log.error("Unexpected error processing specialization create event for specId: {}. Error: {}",
                    event.getId(), e.getMessage(), e);
            handleProcessingFailure(event, acknowledgment);
        }
    }

    @KafkaListener(topics = "${KAFKA_TALENT_ROUTE_UPDATE_TOPIC:talentRoute.update}")
    @Retryable(
            retryFor = {SpecializationProcessingException.class, Exception.class},
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void listenSpecializationUpdate(
            @Payload TalentRouteEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        log.info("Received specialization.update event from topic: {}, partition: {}, offset: {}, specId: {}",
                topic, partition, offset, event.getId());

        try {
            // Validate event
            validateSpecializationEvent(event);

            // Process the specialization update event
            specializationService.updateSpecialization(event);

            log.info("Successfully processed specialization update event for specId: {}",
                    event.getId());

            // Acknowledge message only after successful processing
            acknowledgment.acknowledge();

        } catch (SpecializationProcessingException e) {
            log.error("Failed to process specialization update event for specId: {}. Error: {}",
                    event.getId(), e.getMessage(), e);
            handleProcessingFailure(event, acknowledgment);

        } catch (Exception e) {
            log.error("Unexpected error processing specialization update event for specId: {}. Error: {}",
                    event.getId(), e.getMessage(), e);
            handleProcessingFailure(event, acknowledgment);
        }
    }

    private void validateSpecializationEvent(TalentRouteEvent event) {
        log.debug("Validating specialization event: {}", event);

        if (event == null) {
            throw new SpecializationProcessingException("Specialization event cannot be null");
        }

        if (event.getId() == null) {
            throw new SpecializationProcessingException("Specialization event must contain a valid talent route ID");
        }

        if (event.getName() == null || event.getName().trim().isEmpty()) {
            throw new SpecializationProcessingException("Specialization event must contain a valid talent route name");
        }

        log.debug("Specialization event validation successful for specId: {}", event.getId());
    }

    private void handleProcessingFailure(TalentRouteEvent event, Acknowledgment acknowledgment) {
        String specId = event.getId().toString();

        log.error("Processing failed for specialization event with specId: {}. Sending to DLT topic: {}",
                specId, talentRouteDltTopic);

        try {
            // Send to Dead Letter Topic for manual review/reprocessing
            kafkaTemplate.send(talentRouteDltTopic, specId, event)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            log.info("Successfully sent failed specialization event to DLT for specId: {}", specId);
                        } else {
                            log.error("Failed to send specialization event to DLT for specId: {}", specId, ex);
                        }
                    });

            // Acknowledge the original message to prevent infinite retries
            acknowledgment.acknowledge();

        } catch (Exception dltException) {
            log.error("Critical: Failed to send specialization message to DLT for specId: {}. Message will be retried by Kafka",
                    specId, dltException);
        }
    }
}
