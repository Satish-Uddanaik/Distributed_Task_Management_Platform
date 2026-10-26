package com.Task.Taskflow.dto;

import java.time.LocalDateTime;

public record TaskCreatedEvent(
        String eventType,
        Long taskId,
        String title,
        Long userId,
        LocalDateTime createdAt
) {
}
