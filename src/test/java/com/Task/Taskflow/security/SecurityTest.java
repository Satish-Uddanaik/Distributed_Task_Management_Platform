package com.Task.Taskflow.security;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void shouldRejectRequestWithoutJwt() throws Exception {

        mockMvc.perform(
                        get("/api/tasks/user/1")
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectInvalidJwt() throws Exception {

        mockMvc.perform(
                        get("/api/tasks/user/1")
                                .header(
                                        "Authorization",
                                        "Bearer invalid-token"
                                )
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAcceptValidJwt() throws Exception {

        // Arrange
        UserDetails userDetails =
                User.withUsername("user@gmail.com")
                        .password("password")
                        .roles("USER")
                        .build();

        when(customUserDetailsService
                .loadUserByUsername("user@gmail.com"))
                .thenReturn(userDetails);

        String token = jwtService.generateToken(userDetails);

        // Act + Assert
        mockMvc.perform(
                        get("/api/tasks/user/1")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isForbidden());
    }
}