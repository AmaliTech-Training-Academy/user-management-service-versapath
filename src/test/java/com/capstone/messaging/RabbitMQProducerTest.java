package com.capstone.messaging;

import org.common.event.ProduceUserEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.UUID;

import static com.capstone.config.RabbitMQConfig.USER_EVENT_QUEUE;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RabbitMQProducerTest {
    @Mock
    private RabbitTemplate rabbitTemplate;

    @Test
    void shouldSendUserEventToCorrectQueue() {
        // Given
        RabbitMQProducer producer = new RabbitMQProducer(rabbitTemplate);

        ProduceUserEvent userEvent = ProduceUserEvent.builder()
                .versapathUserId(UUID.randomUUID())
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .username("johndoe")
                .build();

        // When
        producer.sendUserEvent(userEvent);

        // Then
        verify(rabbitTemplate).convertAndSend(USER_EVENT_QUEUE, userEvent);
    }
}
