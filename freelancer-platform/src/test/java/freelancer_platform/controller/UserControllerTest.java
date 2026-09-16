package freelancer_platform.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import freelancer_platform.dto.AuthResponse;
import freelancer_platform.dto.LoginRequest;
import freelancer_platform.dto.UserResponse;
import freelancer_platform.entity.User;
import freelancer_platform.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @Test
    @DisplayName("POST /users/register - successful registration returns 201 Created")
    void testRegisterSuccess() throws Exception {
        User user = new User("John Doe", "john@example.com", "secure123", "CLIENT");
        UserResponse response = new UserResponse(1L, "John Doe", "john@example.com", "CLIENT");

        when(userService.registerUser(any(User.class))).thenReturn(response);

        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.role").value("CLIENT"))
                .andExpect(jsonPath("$.password").doesNotExist());

        verify(userService, times(1)).registerUser(any(User.class));
    }

    @Test
    @DisplayName("POST /users/register - validation failure when fields are blank returns 400 Bad Request")
    void testRegisterValidationFailureBlankFields() throws Exception {
        User invalidUser = new User("", "", "", "");

        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.password").exists())
                .andExpect(jsonPath("$.errors.role").exists());

        verifyNoInteractions(userService);
    }

    @Test
    @DisplayName("POST /users/register - validation failure when email format is invalid returns 400")
    void testRegisterValidationFailureInvalidEmail() throws Exception {
        User invalidUser = new User("Jane", "not-an-email", "pass123", "FREELANCER");

        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.email").exists());

        verifyNoInteractions(userService);
    }

    @Test
    @DisplayName("POST /users/login - successful login returns 200 OK with AuthResponse")
    void testLoginSuccess() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("john@example.com");
        request.setPassword("secure123");

        AuthResponse authResponse = new AuthResponse("mock.jwt.token", "john@example.com", "CLIENT");
        when(userService.login(any(LoginRequest.class))).thenReturn(Optional.of(authResponse));

        mockMvc.perform(post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock.jwt.token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.role").value("CLIENT"));

        verify(userService, times(1)).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("POST /users/login - invalid credentials returns 401 Unauthorized")
    void testLoginFailureInvalidCredentials() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("john@example.com");
        request.setPassword("wrongPassword");

        when(userService.login(any(LoginRequest.class))).thenReturn(Optional.empty());

        mockMvc.perform(post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Invalid email or password"));

        verify(userService, times(1)).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("POST /users/login - validation failure on empty request body returns 400")
    void testLoginValidationFailure() throws Exception {
        LoginRequest invalidRequest = new LoginRequest(); // blank email & password

        mockMvc.perform(post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.password").exists());

        verifyNoInteractions(userService);
    }
}
