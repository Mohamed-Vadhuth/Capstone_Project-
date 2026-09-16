package freelancer_platform.controller;

import freelancer_platform.dto.ReviewRequest;
import freelancer_platform.dto.ReviewResponse;
import freelancer_platform.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    // Submit a review for a completed project
    @PostMapping
    public ResponseEntity<ReviewResponse> createReview(
            @Valid @RequestBody ReviewRequest request,
            Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        ReviewResponse response = reviewService.createReview(request, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Get all reviews for a user
    @GetMapping("/user/{email}")
    public ResponseEntity<List<ReviewResponse>> getReviewsForUser(@PathVariable String email) {
        List<ReviewResponse> reviews = reviewService.getReviewsForUser(email);
        return ResponseEntity.ok(reviews);
    }

    // Get all reviews for a project
    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<ReviewResponse>> getReviewsForProject(@PathVariable Long projectId) {
        List<ReviewResponse> reviews = reviewService.getReviewsForProject(projectId);
        return ResponseEntity.ok(reviews);
    }
}
