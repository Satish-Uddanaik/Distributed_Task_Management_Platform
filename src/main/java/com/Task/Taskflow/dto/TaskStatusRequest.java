package com.Task.Taskflow.dto;

import com.Task.Taskflow.entity.TaskStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskStatusRequest {

    @NotNull(message = "Status is required")
    private TaskStatus status;
}