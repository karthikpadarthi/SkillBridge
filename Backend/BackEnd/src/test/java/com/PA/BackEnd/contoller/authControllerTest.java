package com.PA.BackEnd.contoller;

import com.PA.BackEnd.dto.authResponse;
import com.PA.BackEnd.dto.loginRequest;
import com.PA.BackEnd.dto.registerRequest;
import com.PA.BackEnd.service.authService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class authControllerTest {

    @Mock
    private authService authService;

    @InjectMocks
    private authController authController;

    @Test
    void register_shouldDelegateAndReturnServicePayload() {
        registerRequest request = new registerRequest();
        request.setEmail("user@test.com");
        request.setPassword("Password@123");
        authResponse expected = new authResponse("token-1", "user@test.com");

        when(authService.register(any(registerRequest.class))).thenReturn(expected);

        authResponse actual = authController.register(request);

        assertThat(actual.getToken()).isEqualTo("token-1");
        assertThat(actual.getUserId()).isEqualTo("user@test.com");
        verify(authService).register(request);
    }

    @Test
    void login_whenServiceThrows_shouldPropagateException() {
        loginRequest request = new loginRequest();
        request.setEmail("user@test.com");
        request.setPassword("wrong");
        when(authService.login(any(loginRequest.class)))
                .thenThrow(new IllegalArgumentException("Invalid email or password"));

        assertThatThrownBy(() -> authController.login(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid email or password");
    }
}

