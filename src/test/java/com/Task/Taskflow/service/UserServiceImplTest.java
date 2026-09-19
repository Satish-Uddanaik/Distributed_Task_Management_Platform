package com.Task.Taskflow.service;


import com.Task.Taskflow.dto.UserRequest;
import com.Task.Taskflow.dto.UserResponse;
import com.Task.Taskflow.entity.Role;
import com.Task.Taskflow.entity.User;
import com.Task.Taskflow.exception.UserNotFoundException;
import com.Task.Taskflow.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void shouldCreateUserSuccessfully() {

        // Arrange
        UserRequest request = new UserRequest();

        request.setName("John");
        request.setEmail("john@gmail.com");
        request.setPassword("123456");

        when(passwordEncoder.encode("123456"))
                .thenReturn("hashedPassword");

        User savedUser = User.builder()
                .id(1L)
                .name("John")
                .email("john@gmail.com")
                .password("hashedPassword")
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.save(any(User.class)))
                .thenReturn(savedUser);

        // Act
        UserResponse response =
                userService.createUser(request);

        // Assert
        assertNotNull(response);

        assertEquals(1L, response.getId());
        assertEquals("John", response.getName());
        assertEquals("john@gmail.com", response.getEmail());
        assertEquals(Role.USER, response.getRole());

        verify(passwordEncoder)
                .encode("123456");

        verify(userRepository)
                .save(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {

        // Arrange
        when(userRepository.findById(99L))
                .thenReturn(Optional.empty());

        // Act + Assert
        UserNotFoundException exception =
                assertThrows(
                        UserNotFoundException.class,
                        () -> userService.getUserById(99L)
                );

        assertEquals(
                "User not found with id: 99",
                exception.getMessage()
        );

        verify(userRepository)
                .findById(99L);
    }

    @Test
    void shouldGetAllUsers() {

        // Arrange
        User user1 = User.builder()
                .id(1L)
                .name("John")
                .email("john@gmail.com")
                .password("hash1")
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .build();

        User user2 = User.builder()
                .id(2L)
                .name("Admin")
                .email("admin@gmail.com")
                .password("hash2")
                .role(Role.ADMIN)
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.findAll())
                .thenReturn(List.of(user1, user2));

        // Act
        List<UserResponse> users =
                userService.getAllUsers();

        // Assert
        assertNotNull(users);

        assertEquals(2, users.size());

        assertEquals("John", users.get(0).getName());
        assertEquals("Admin", users.get(1).getName());

        verify(userRepository)
                .findAll();
    }
}
