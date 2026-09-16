package freelancer_platform.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import freelancer_platform.dto.ReviewRequest;
import freelancer_platform.dto.ReviewResponse;
import freelancer_platform.security.JwtUtil;
import freelancer_platform.service.ReviewService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @MockitoBean
    private ReviewService reviewService;

    @Test
    @DisplayName("POST /reviews - without JWT returns 401 Unauthorized")
    void testCreateReviewWithoutJwt() throws Exception {
        ReviewRequest request = new ReviewRequest(1L, 5, "Good project");

        mockMvc.perform(post("/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /reviews - with valid JWT creates review and returns 201 Created")
    void testCreateReviewWithJwt() throws Exception {
        String token = jwtUtil.generateToken("client@test.com", "CLIENT");
        ReviewRequest request = new ReviewRequest(1L, 5, "Excellent work delivered on time!");

        ReviewResponse response = new ReviewResponse(10L, 1L, "client@test.com", "Client Name",
                "dev@test.com", 5, "Excellent work delivered on time!", LocalDateTime.now());

        when(reviewService.createReview(any(ReviewRequest.class), eq("client@test.com"))).thenReturn(response);

        mockMvc.perform(post("/reviews")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.reviewerEmail").value("client@test.com"));
    }

    @Test
    @DisplayName("POST /reviews - validation failure on invalid rating outside 1..5 returns 400 Bad Request")
    void testCreateReviewValidationFailure() throws Exception {
        String token = jwtUtil.generateToken("client@test.com", "CLIENT");
        ReviewRequest invalidRequest = new ReviewRequest(1L, 10, "Invalid rating");

        mockMvc.perform(post("/reviews")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.rating").exists());
    }

    @Test
    @DisplayName("GET /reviews/user/{email} - public endpoint returns user reviews")
    void testGetReviewsForUser() throws Exception {
        ReviewResponse review = new ReviewResponse(1L, 2L, "reviewer@test.com", "Reviewer",
                "user@test.com", 5, "Very good", LocalDateTime.now());

        when(reviewService.getReviewsForUser("user@test.com")).thenReturn(List.of(review));

        mockMvc.perform(get("/reviews/user/user@test.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].reviewedUserEmail").value("user@test.com"));
    }
}
