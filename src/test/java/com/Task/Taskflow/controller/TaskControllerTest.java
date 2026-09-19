package com.Task.Taskflow.controller;



import com.Task.Taskflow.dto.TaskResponse;
import com.Task.Taskflow.entity.TaskPriority;
import com.Task.Taskflow.entity.TaskStatus;
import com.Task.Taskflow.security.CustomUserDetailsService;
import com.Task.Taskflow.security.JwtService;
import com.Task.Taskflow.service.TaskService;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest(TaskController.class)
@org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc(addFilters = false)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void shouldGetTaskById() throws Exception {

        // Arrange
        TaskResponse response = TaskResponse.builder()
                .id(1L)
                .title("Learn Spring Boot")
                .description("Learn Spring Security")
                .status(TaskStatus.IN_PROGRESS)
                .priority(TaskPriority.HIGH)
                .dueDate(LocalDate.now())
                .userId(1L)
                .build();

        when(taskService.getTaskById(1L))
                .thenReturn(response);

        // Act + Assert
        mockMvc.perform(
                        get("/api/tasks/1")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title")
                        .value("Learn Spring Boot"))
                .andExpect(jsonPath("$.status")
                        .value("IN_PROGRESS"))
                .andExpect(jsonPath("$.priority")
                        .value("HIGH"))
                .andExpect(jsonPath("$.userId")
                        .value(1));
    }


    @Test
    void shouldUpdateTaskStatus() throws Exception {

        // Arrange
        TaskResponse response = TaskResponse.builder()
                .id(1L)
                .title("Learn Spring Boot")
                .status(TaskStatus.COMPLETED)
                .priority(TaskPriority.HIGH)
                .userId(1L)
                .build();

        when(taskService.updateTaskStatus(
                1L,
                TaskStatus.COMPLETED
        )).thenReturn(response);

        String requestBody = """
                {
                    "status": "COMPLETED"
                }
                """;

        // Act + Assert
        mockMvc.perform(
                        patch("/api/tasks/1/status")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status")
                        .value("COMPLETED"));
    }
}