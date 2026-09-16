package freelancer_platform.controller;

import freelancer_platform.dto.LoginRequest;
import freelancer_platform.dto.UpdateProfileRequest;
import freelancer_platform.dto.UserProfileDto;
import freelancer_platform.dto.UserResponse;
import freelancer_platform.entity.User;
import freelancer_platform.exception.ErrorResponse;
import freelancer_platform.service.UserService;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Registration
    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody User user) {
        UserResponse response = userService.registerUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Login
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        return userService.login(request)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), "Invalid email or password")));
    }

    // Get current authenticated user profile
    @GetMapping("/profile")
    public ResponseEntity<UserProfileDto> getProfile(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        UserProfileDto profile = userService.getProfile(principal.getName());
        return ResponseEntity.ok(profile);
    }

    // Update current authenticated user profile
    @PutMapping("/profile")
    public ResponseEntity<UserProfileDto> updateProfile(
            Principal principal,
            @Valid @RequestBody UpdateProfileRequest request) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        UserProfileDto updated = userService.updateProfile(principal.getName(), request);
        return ResponseEntity.ok(updated);
    }

    // View any user's public profile and rating
    @GetMapping("/{email}/profile")
    public ResponseEntity<UserProfileDto> getUserPublicProfile(@PathVariable String email) {
        UserProfileDto profile = userService.getProfile(email);
        return ResponseEntity.ok(profile);
    }
}