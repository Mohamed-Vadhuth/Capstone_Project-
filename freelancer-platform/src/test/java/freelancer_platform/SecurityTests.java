package freelancer_platform;

import freelancer_platform.dto.LoginRequest;
import freelancer_platform.entity.Project;
import freelancer_platform.entity.Proposal;
import freelancer_platform.entity.User;
import freelancer_platform.repository.UserRepository;
import freelancer_platform.security.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import freelancer_platform.service.EmailService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class SecurityTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @MockitoBean
    private EmailService emailService;

    @Test
    @DisplayName("1. BCrypt: Password should be hashed, not stored plain text")
    void testBCryptPasswordHashing() throws Exception {
        String testEmail = "bcrypt_test_" + System.currentTimeMillis() + "@test.com";
        User user = new User("BCrypt Tester", testEmail, "rawPassword123", "CLIENT");

        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.password").doesNotExist()); // Password never returned

        User saved = userRepository.findByEmail(testEmail).orElseThrow();
        assertNotEquals("rawPassword123", saved.getPassword());
        assertTrue(passwordEncoder.matches("rawPassword123", saved.getPassword()));
    }

    @Test
    @DisplayName("2. Valid Login returns JWT token and user info, without password")
    void testLoginSuccessReturnsJwt() throws Exception {
        String testEmail = "login_success_" + System.currentTimeMillis() + "@test.com";
        User user = new User("Login Tester", testEmail, "testPass123", "FREELANCER");

        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isCreated());

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(testEmail);
        loginRequest.setPassword("testPass123");

        mockMvc.perform(post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.email").value(testEmail))
                .andExpect(jsonPath("$.role").value("FREELANCER"))
                .andExpect(jsonPath("$.password").doesNotExist());

        verify(emailService, times(1)).sendLoginNotification(eq(testEmail), eq("Login Tester"), any());
    }

    @Test
    @DisplayName("3. Login with invalid password returns 401 Unauthorized")
    void testLoginWithInvalidPassword() throws Exception {
        String testEmail = "wrong_pass_" + System.currentTimeMillis() + "@test.com";
        User user = new User("WrongPass Tester", testEmail, "correctPass", "CLIENT");

        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isCreated());

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(testEmail);
        loginRequest.setPassword("incorrectPass");

        mockMvc.perform(post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password"));

        verify(emailService, never()).sendLoginNotification(eq(testEmail), any(), any());
    }

    @Test
    @DisplayName("4. Protected endpoint without JWT returns 401 Unauthorized")
    void testProtectedEndpointWithoutJwt() throws Exception {
        Project project = new Project("Protected Project", "Description", 10000, "Web");

        mockMvc.perform(post("/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(project)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("5. Protected endpoint with valid JWT succeeds (201 Created)")
    void testProtectedEndpointWithValidJwt() throws Exception {
        String token = jwtUtil.generateToken("auth_client@test.com", "CLIENT");
        Project project = new Project("Auth Project", "Valid JWT test", 25000, "Dev");

        mockMvc.perform(post("/projects")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(project)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Auth Project"));
    }

    @Test
    @DisplayName("6. Protected endpoint with invalid JWT returns 401 Unauthorized")
    void testProtectedEndpointWithInvalidJwt() throws Exception {
        Project project = new Project("Invalid JWT Project", "Description", 10000, "Web");

        mockMvc.perform(post("/projects")
                        .header("Authorization", "Bearer invalid.jwt.token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(project)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("7. Public endpoints remain accessible without JWT")
    void testPublicEndpointsAccessible() throws Exception {
        // Health
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));

        // Projects browsing
        mockMvc.perform(get("/projects"))
                .andExpect(status().isOk());

        // Proposals browsing
        mockMvc.perform(get("/proposals/project/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("8. JWT contains correct email and role claim without sensitive info")
    void testJwtClaims() {
        String token = jwtUtil.generateToken("claimstest@test.com", "CLIENT");

        assertTrue(jwtUtil.validateToken(token));
        assertEquals("claimstest@test.com", jwtUtil.extractEmail(token));
        assertEquals("CLIENT", jwtUtil.extractRole(token));
    }

    @Test
    @DisplayName("9. Protected proposal endpoint without JWT returns 401 Unauthorized")
    void testProposalEndpointWithoutJwt() throws Exception {
        Proposal proposal = new Proposal(1L, "freelancer@test.com", "Test cover letter", 5000);

        mockMvc.perform(post("/proposals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(proposal)))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(put("/proposals/1/accept"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(put("/proposals/1/reject"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("10. Expired JWT is rejected and returns false on validation")
    void testExpiredJwtValidation() {
        // JwtUtil with 0 expiration ms or already expired
        JwtUtil expiredJwtUtil = new JwtUtil("c2VjdXJlX2RldmVsb3BtZW50X2tleV9mb3JfZnJlZWxhbmNlcl9wbGF0Zm9ybV9jYXBzdG9uZV9wcm9qZWN0XzIwMjZfMjU2Yml0cw==", -1000);
        String expiredToken = expiredJwtUtil.generateToken("expired@test.com", "CLIENT");

        assertFalse(jwtUtil.validateToken(expiredToken));
    }
}

