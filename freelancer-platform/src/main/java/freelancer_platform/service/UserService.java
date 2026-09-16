package freelancer_platform.service;

import freelancer_platform.dto.AuthResponse;
import freelancer_platform.dto.LoginRequest;
import freelancer_platform.dto.UpdateProfileRequest;
import freelancer_platform.dto.UserProfileDto;
import freelancer_platform.dto.UserResponse;
import freelancer_platform.entity.Review;
import freelancer_platform.entity.User;
import freelancer_platform.exception.ResourceNotFoundException;
import freelancer_platform.repository.ReviewRepository;
import freelancer_platform.repository.UserRepository;
import freelancer_platform.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Autowired(required = false)
    private ReviewRepository reviewRepository;

    // Preserved constructor for existing UserServiceTest
    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Autowired
    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil,
            @Autowired(required = false) ReviewRepository reviewRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.reviewRepository = reviewRepository;
    }

    // Register user with BCrypt hashed password
    public UserResponse registerUser(User user) {
        // Hash password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User saved = userRepository.save(user);
        return new UserResponse(saved.getId(), saved.getName(), saved.getEmail(), saved.getRole());
    }

    // Authenticate user login and issue JWT
    public Optional<AuthResponse> login(LoginRequest request) {
        return userRepository.findByEmail(request.getEmail())
                .filter(user -> passwordEncoder.matches(request.getPassword(), user.getPassword()))
                .map(user -> {
                    String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
                    return new AuthResponse(token, user.getEmail(), user.getRole());
                });
    }

    // Find user by email
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // Get user profile with rating stats
    public UserProfileDto getProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        List<Review> reviews = (reviewRepository != null)
                ? reviewRepository.findByReviewedUserEmail(email)
                : Collections.emptyList();
        double avgRating = 0.0;
        int reviewCount = reviews.size();
        if (reviewCount > 0) {
            double sum = reviews.stream().mapToInt(Review::getRating).sum();
            avgRating = Math.round((sum / reviewCount) * 10.0) / 10.0;
        }

        return new UserProfileDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getBio(),
                user.getSkills(),
                user.getExperience(),
                user.getPortfolioUrl(),
                avgRating,
                reviewCount
        );
    }

    // Update user profile
    public UserProfileDto updateProfile(String email, UpdateProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        if (request.getName() != null && !request.getName().isBlank()) {
            user.setName(request.getName().trim());
        }
        user.setBio(request.getBio());
        user.setSkills(request.getSkills());
        user.setExperience(request.getExperience());
        user.setPortfolioUrl(request.getPortfolioUrl());

        userRepository.save(user);
        return getProfile(email);
    }
}
