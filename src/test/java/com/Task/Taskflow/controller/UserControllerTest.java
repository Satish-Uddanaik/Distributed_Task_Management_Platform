package com.Task.Taskflow.controller;

import com.Task.Taskflow.dto.UserResponse;
import com.Task.Taskflow.entity.Role;
import com.Task.Taskflow.security.CustomUserDetailsService;
import com.Task.Taskflow.service.UserService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.Task.Taskflow.security.JwtService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void shouldGetUserById() throws Exception {

        UserResponse response = UserResponse.builder()
                .id(1L)
                .name("John")
                .email("john@gmail.com")
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .build();

        when(userService.getUserById(1L))
                .thenReturn(response);

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("John"))
                .andExpect(jsonPath("$.email").value("john@gmail.com"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void shouldCreateUser() throws Exception {

        UserResponse response = UserResponse.builder()
                .id(1L)
                .name("John")
                .email("john@gmail.com")
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .build();

        when(userService.createUser(any()))
                .thenReturn(response);

        String requestBody = """
                {
                    "name": "John",
                    "email": "john@gmail.com",
                    "password": "123456"
                }
                """;

        mockMvc.perform(
                        post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("John"))
                .andExpect(jsonPath("$.email").value("john@gmail.com"))
                .andExpect(jsonPath("$.role").value("USER"));
    }


    @Test
    void shouldRejectInvalidUser() throws Exception {

        String requestBody = """
                {
                    "name": "",
                    "email": "wrong-email",
                    "password": "123"
                }
                """;

        mockMvc.perform(
                        post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest());
    }


    @Test
    void shouldGetAllUsers() throws Exception {

        UserResponse user1 = UserResponse.builder()
                .id(1L)
                .name("John")
                .email("john@gmail.com")
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .build();

        UserResponse user2 = UserResponse.builder()
                .id(2L)
                .name("Admin")
                .email("admin@gmail.com")
                .role(Role.ADMIN)
                .createdAt(LocalDateTime.now())
                .build();

        when(userService.getAllUsers())
                .thenReturn(List.of(user1, user2));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("John"))
                .andExpect(jsonPath("$[1].name").value("Admin"));
    }
}