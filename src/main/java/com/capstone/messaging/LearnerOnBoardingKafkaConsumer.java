package com.capstone.messaging;

import org.common.event.LearnerOnBoardingEvent;
import com.capstone.model.User;
import com.capstone.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class LearnerOnBoardingKafkaConsumer {

    private final UserRepository userRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${KAFKA_LEARNER_ONBOARD_DLT_TOPIC:learner.onboard.dlt}")
    private String learnerOnboardDltTopic;

    @KafkaListener(topics = "${KAFKA_LEARNER_ONBOARD_TOPIC:learner.onboard}")
    @Retryable(
            retryFor = {RuntimeException.class, Exception.class},
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    @Transactional
    public void listenLearnerOnBoarding(
            @Payload LearnerOnBoardingEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        log.info("Received learner.onboard event from topic: {}, partition: {}, offset: {}, learnerId: {}",
                topic, partition, offset, event.getLearnerId());

        try {
            // Validate event
            validateLearnerOnBoardingEvent(event);

            // Process the learner onboarding event
            processLearnerOnBoarding(event);

            log.info("Successfully processed learner onboarding event for learnerId: {}",
                    event.getLearnerId());

            // Acknowledge message only after successful processing
            acknowledgment.acknowledge();

        } catch (RuntimeException e) {
            log.error("Failed to process learner onboarding event for learnerId: {}. Error: {}",
                    event.getLearnerId(), e.getMessage(), e);
            handleProcessingFailure(event, acknowledgment);

        } catch (Exception e) {
            log.error("Unexpected error processing learner onboarding event for learnerId: {}. Error: {}",
                    event.getLearnerId(), e.getMessage(), e);
            handleProcessingFailure(event, acknowledgment);
        }
    }

    private void validateLearnerOnBoardingEvent(LearnerOnBoardingEvent event) {
        log.debug("Validating learner onboarding event: {}", event);

        if (event == null) {
            throw new RuntimeException("Learner onboarding event cannot be null");
        }

        if (event.getLearnerId() == null) {
            throw new RuntimeException("Learner onboarding event must contain a valid learner ID");
        }

        log.debug("Learner onboarding event validation successful for learnerId: {}", event.getLearnerId());
    }

    private void processLearnerOnBoarding(LearnerOnBoardingEvent event) {
        UUID learnerId = event.getLearnerId();
        boolean requiresOnboarding = event.isRequiresOnboarding();

        log.info("Processing onboarding for learner: {}, requiresOnboarding: {}", learnerId, requiresOnboarding);

        // Find the user
        User user = userRepository.findById(learnerId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + learnerId));

        // Update onBoardAt field based on the event
        if (!requiresOnboarding) {
            // If user doesn't require onboarding, mark as completed
            if (user.getOnBoardAt() == null) {
                log.info("Marking onboarding as completed for learner: {}", learnerId);
                user.setOnBoardAt(LocalDateTime.now());
                userRepository.save(user);
            } else {
                log.info("Learner {} already completed onboarding at: {}", learnerId, user.getOnBoardAt());
            }
        }else {
            log.info("Learner {} requires onboarding. No changes made to onBoardAt field.", learnerId);
        }

        log.info("Successfully updated onboarding status for learner: {}", learnerId);
    }

    private void handleProcessingFailure(LearnerOnBoardingEvent event, Acknowledgment acknowledgment) {
        String learnerId = event.getLearnerId().toString();

        log.error("Processing failed for learner onboarding event with learnerId: {}. Sending to DLT topic: {}",
                learnerId, learnerOnboardDltTopic);

        try {
            // Send to Dead Letter Topic for manual review/reprocessing
            kafkaTemplate.send(learnerOnboardDltTopic, learnerId, event)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            log.info("Successfully sent failed learner onboarding event to DLT for learnerId: {}", learnerId);
                        } else {
                            log.error("Failed to send learner onboarding event to DLT for learnerId: {}", learnerId, ex);
                        }
                    });

            // Acknowledge the original message to prevent infinite retries
            acknowledgment.acknowledge();

        } catch (Exception dltException) {
            log.error("Critical: Failed to send learner onboarding message to DLT for learnerId: {}. Message will be retried by Kafka",
                    learnerId, dltException);
        }
    }
}
