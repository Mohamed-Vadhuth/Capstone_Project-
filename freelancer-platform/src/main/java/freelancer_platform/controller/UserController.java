package freelancer_platform.controller;

import freelancer_platform.dto.LoginRequest;
import freelancer_platform.entity.User;
import freelancer_platform.repository.UserRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Registration
    @PostMapping("/register")
    public User registerUser(@RequestBody User user) {
        return userRepository.save(user);
    }

    // Login
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest request) {

        return userRepository.findByEmail(request.getEmail())
                .map(user -> {
                    if (user.getPassword().equals(request.getPassword())) {
                        return ResponseEntity.ok("Login successful!");
                    } else {
                        return ResponseEntity.status(401)
                                .body("Invalid password");
                    }
                })
                .orElse(
                        ResponseEntity.status(401)
                                .body("User not found")
                );
    }
}