package com.Task.Taskflow.kafka;

import com.Task.Taskflow.dto.TaskCreatedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Service
public class KafkaProducerService {

    private static final String TOPIC = "task-created";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public KafkaProducerService(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void sendMessage(String message) {
        kafkaTemplate.send(TOPIC, message);
        System.out.println("Message submitted: " + message);
    }

    public void publishTaskCreated(TaskCreatedEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);

            kafkaTemplate.send(
                    TOPIC,
                    event.taskId().toString(),
                    payload
            );

            System.out.println("Task-created event submitted: " + payload);

        } catch (JacksonException e) {
            throw new IllegalStateException(
                    "Failed to serialize task-created event", e
            );
        }
    }
}
