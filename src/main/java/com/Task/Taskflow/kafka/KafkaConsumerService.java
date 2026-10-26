package com.Task.Taskflow.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {

    @KafkaListener(
            topics = "task-created",
            groupId = "taskflow-group"
    )
    public void consumeMessage(String message) {
        System.out.println("Message received: " + message);
    }
}
