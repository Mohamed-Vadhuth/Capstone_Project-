package freelancer_platform.service;

import freelancer_platform.dto.AuthResponse;
import freelancer_platform.dto.LoginRequest;
import freelancer_platform.dto.UserResponse;
import freelancer_platform.entity.User;
import freelancer_platform.repository.UserRepository;
import freelancer_platform.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserService userService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User("Alice", "alice@example.com", "plainPass123", "CLIENT");
    }

    @Test
    @DisplayName("registerUser: encodes password and saves user via repository")
    void testRegisterUserSuccess() {
        // Arrange
        when(passwordEncoder.encode("plainPass123")).thenReturn("encodedPass_xyz");
        User savedUser = new User("Alice", "alice@example.com", "encodedPass_xyz", "CLIENT");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        UserResponse response = userService.registerUser(sampleUser);

        // Assert
        assertNotNull(response);
        assertEquals("Alice", response.getName());
        assertEquals("alice@example.com", response.getEmail());
        assertEquals("CLIENT", response.getRole());
        assertEquals("encodedPass_xyz", sampleUser.getPassword());

        verify(passwordEncoder, times(1)).encode("plainPass123");
        verify(userRepository, times(1)).save(sampleUser);
    }

    @Test
    @DisplayName("registerUser: preserves role correctly (FREELANCER)")
    void testRegisterUserFreelancerRole() {
        // Arrange
        User freelancer = new User("Bob", "bob@example.com", "secret456", "FREELANCER");
        when(passwordEncoder.encode("secret456")).thenReturn("hashed_secret");
        when(userRepository.save(any(User.class))).thenReturn(freelancer);

        // Act
        UserResponse response = userService.registerUser(freelancer);

        // Assert
        assertEquals("FREELANCER", response.getRole());
        assertEquals("bob@example.com", response.getEmail());
        verify(passwordEncoder).encode("secret456");
        verify(userRepository).save(freelancer);
    }

    @Test
    @DisplayName("login: returns AuthResponse with JWT when credentials match")
    void testLoginSuccess() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("alice@example.com");
        request.setPassword("plainPass123");

        User existingUser = new User("Alice", "alice@example.com", "hashed_pass", "CLIENT");
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("plainPass123", "hashed_pass")).thenReturn(true);
        when(jwtUtil.generateToken("alice@example.com", "CLIENT")).thenReturn("jwt_sample_token");

        // Act
        Optional<AuthResponse> result = userService.login(request);

        // Assert
        assertTrue(result.isPresent());
        AuthResponse auth = result.get();
        assertEquals("jwt_sample_token", auth.getToken());
        assertEquals("alice@example.com", auth.getEmail());
        assertEquals("CLIENT", auth.getRole());
        assertEquals("Bearer", auth.getTokenType());

        verify(userRepository).findByEmail("alice@example.com");
        verify(passwordEncoder).matches("plainPass123", "hashed_pass");
        verify(jwtUtil).generateToken("alice@example.com", "CLIENT");
    }

    @Test
    @DisplayName("login: returns empty Optional when password does not match")
    void testLoginInvalidPassword() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("alice@example.com");
        request.setPassword("wrongPassword");

        User existingUser = new User("Alice", "alice@example.com", "hashed_pass", "CLIENT");
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("wrongPassword", "hashed_pass")).thenReturn(false);

        // Act
        Optional<AuthResponse> result = userService.login(request);

        // Assert
        assertFalse(result.isPresent());
        verify(userRepository).findByEmail("alice@example.com");
        verify(passwordEncoder).matches("wrongPassword", "hashed_pass");
        verifyNoInteractions(jwtUtil);
    }

    @Test
    @DisplayName("login: returns empty Optional when user does not exist")
    void testLoginUserNotFound() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("nonexistent@example.com");
        request.setPassword("anyPassword");

        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act
        Optional<AuthResponse> result = userService.login(request);

        // Assert
        assertFalse(result.isPresent());
        verify(userRepository).findByEmail("nonexistent@example.com");
        verifyNoInteractions(passwordEncoder);
        verifyNoInteractions(jwtUtil);
    }

    @Test
    @DisplayName("findByEmail: returns user when exists")
    void testFindByEmailFound() {
        // Arrange
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(sampleUser));

        // Act
        Optional<User> result = userService.findByEmail("alice@example.com");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Alice", result.get().getName());
        verify(userRepository).findByEmail("alice@example.com");
    }

    @Test
    @DisplayName("findByEmail: returns empty when user does not exist")
    void testFindByEmailNotFound() {
        // Arrange
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userService.findByEmail("unknown@example.com");

        // Assert
        assertFalse(result.isPresent());
        verify(userRepository).findByEmail("unknown@example.com");
    }
}
