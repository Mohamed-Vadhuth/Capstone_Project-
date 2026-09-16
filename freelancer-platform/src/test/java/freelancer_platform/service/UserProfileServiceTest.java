package freelancer_platform.service;

import freelancer_platform.dto.UpdateProfileRequest;
import freelancer_platform.dto.UserProfileDto;
import freelancer_platform.entity.Review;
import freelancer_platform.entity.User;
import freelancer_platform.repository.ReviewRepository;
import freelancer_platform.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserProfileServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private UserService userService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User("John Dev", "john@dev.com", "hashPass", "FREELANCER");
        sampleUser.setBio("Senior Java Engineer");
        sampleUser.setSkills("Java, Spring Boot, MySQL");
        sampleUser.setExperience("7 years");
        sampleUser.setPortfolioUrl("https://johndev.tech");
    }

    @Test
    @DisplayName("getProfile: returns user profile with aggregated rating")
    void testGetProfileWithRating() {
        when(userRepository.findByEmail("john@dev.com")).thenReturn(Optional.of(sampleUser));
        Review r1 = new Review(1L, "client1@test.com", "john@dev.com", 5, "Great work!");
        Review r2 = new Review(2L, "client2@test.com", "john@dev.com", 4, "Good job!");
        when(reviewRepository.findByReviewedUserEmail("john@dev.com")).thenReturn(List.of(r1, r2));

        UserProfileDto profile = userService.getProfile("john@dev.com");

        assertNotNull(profile);
        assertEquals("John Dev", profile.getName());
        assertEquals("john@dev.com", profile.getEmail());
        assertEquals("Senior Java Engineer", profile.getBio());
        assertEquals(4.5, profile.getAverageRating());
        assertEquals(2, profile.getReviewCount());
    }

    @Test
    @DisplayName("updateProfile: updates bio, skills, and portfolio successfully")
    void testUpdateProfile() {
        when(userRepository.findByEmail("john@dev.com")).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);
        when(reviewRepository.findByReviewedUserEmail("john@dev.com")).thenReturn(List.of());

        UpdateProfileRequest request = new UpdateProfileRequest("John Updated", "New Bio", "Go, Python", "8 years", "https://newsite.com");

        UserProfileDto updated = userService.updateProfile("john@dev.com", request);

        assertNotNull(updated);
        assertEquals("John Updated", updated.getName());
        assertEquals("New Bio", updated.getBio());
        assertEquals("Go, Python", updated.getSkills());
        assertEquals("https://newsite.com", updated.getPortfolioUrl());
        verify(userRepository, times(1)).save(sampleUser);
    }
}
