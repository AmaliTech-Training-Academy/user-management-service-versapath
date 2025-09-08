package com.capstone.messaging;

import com.capstone.config.RabbitMQConfig;
import com.capstone.service.UserManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.common.event.UpdateUserEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitMQConsumer {

    private final UserManagementService userManagementService;

    @RabbitListener(queues = RabbitMQConfig.UPDATE_USER_QUEUE)
    public void handleUpdateUserEvent(UpdateUserEvent updateUserEvent) {
        log.info("Received UpdateUserEvent for versapathUserId: {}", updateUserEvent.getVersapathUserId());

        try {
            userManagementService.updateMoodleUserId(
                    updateUserEvent.getVersapathUserId(),
                    updateUserEvent.getMoodleUserId()
            );
        } catch (Exception e) {
            log.error("Failed to process UpdateUserEvent for user: {}", updateUserEvent.getVersapathUserId(), e);
        }
    }
}
