package com.capstone.messaging;

import lombok.RequiredArgsConstructor;
import org.common.event.ProduceUserEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import static com.capstone.config.RabbitMQConfig.USER_EVENT_QUEUE;

@Component
@RequiredArgsConstructor
public class RabbitMQProducer {

    private final RabbitTemplate rabbitTemplate;

    public void sendUserEvent(ProduceUserEvent userEvent) {
        rabbitTemplate.convertAndSend(USER_EVENT_QUEUE, userEvent);
    }
}
