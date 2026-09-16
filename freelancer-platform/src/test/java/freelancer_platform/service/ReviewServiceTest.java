package freelancer_platform.service;

import freelancer_platform.dto.ReviewRequest;
import freelancer_platform.dto.ReviewResponse;
import freelancer_platform.entity.Project;
import freelancer_platform.entity.Proposal;
import freelancer_platform.entity.Review;
import freelancer_platform.repository.ProjectRepository;
import freelancer_platform.repository.ProposalRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProposalRepository proposalRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReviewService reviewService;

    private Project completedProject;
    private Proposal acceptedProposal;

    @BeforeEach
    void setUp() {
        completedProject = new Project("Mobile App", "iOS App Development", 15000, "Mobile", "client@test.com");
        completedProject.setStatus("COMPLETED");

        acceptedProposal = new Proposal(1L, "dev@test.com", "Ready to code", 15000);
        acceptedProposal.setStatus("ACCEPTED");
    }

    @Test
    @DisplayName("createReview: Client successfully reviews freelancer for completed project")
    void testClientReviewsFreelancerSuccess() {
        ReviewRequest request = new ReviewRequest(1L, 5, "Outstanding deliverables!");
        when(projectRepository.findById(1L)).thenReturn(Optional.of(completedProject));
        when(reviewRepository.existsByProjectIdAndReviewerEmail(any(), any())).thenReturn(false);
        when(proposalRepository.findByProjectIdAndStatus(any(), eq("ACCEPTED"))).thenReturn(Optional.of(acceptedProposal));

        Review savedReview = new Review(1L, "client@test.com", "dev@test.com", 5, "Outstanding deliverables!");
        when(reviewRepository.save(any(Review.class))).thenReturn(savedReview);

        ReviewResponse response = reviewService.createReview(request, "client@test.com");

        assertNotNull(response);
        assertEquals(5, response.getRating());
        assertEquals("client@test.com", response.getReviewerEmail());
        assertEquals("dev@test.com", response.getReviewedUserEmail());
        verify(reviewRepository, times(1)).save(any(Review.class));
    }

    @Test
    @DisplayName("createReview: Freelancer successfully reviews client for completed project")
    void testFreelancerReviewsClientSuccess() {
        ReviewRequest request = new ReviewRequest(1L, 4, "Clear specifications and prompt payment.");
        when(projectRepository.findById(1L)).thenReturn(Optional.of(completedProject));
        when(reviewRepository.existsByProjectIdAndReviewerEmail(any(), any())).thenReturn(false);
        when(proposalRepository.findByProjectIdAndStatus(any(), eq("ACCEPTED"))).thenReturn(Optional.of(acceptedProposal));

        Review savedReview = new Review(1L, "dev@test.com", "client@test.com", 4, "Clear specifications and prompt payment.");
        when(reviewRepository.save(any(Review.class))).thenReturn(savedReview);

        ReviewResponse response = reviewService.createReview(request, "dev@test.com");

        assertNotNull(response);
        assertEquals(4, response.getRating());
        assertEquals("dev@test.com", response.getReviewerEmail());
        assertEquals("client@test.com", response.getReviewedUserEmail());
    }

    @Test
    @DisplayName("createReview: Fails when project is not COMPLETED")
    void testReviewNonCompletedProjectFails() {
        completedProject.setStatus("OPEN");
        ReviewRequest request = new ReviewRequest(1L, 5, "Nice");
        when(projectRepository.findById(1L)).thenReturn(Optional.of(completedProject));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                reviewService.createReview(request, "client@test.com")
        );
        assertTrue(ex.getMessage().contains("COMPLETED"));
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    @DisplayName("createReview: Fails on duplicate review by same reviewer")
    void testDuplicateReviewFails() {
        ReviewRequest request = new ReviewRequest(1L, 5, "Duplicate");
        when(projectRepository.findById(1L)).thenReturn(Optional.of(completedProject));
        when(reviewRepository.existsByProjectIdAndReviewerEmail(any(), eq("client@test.com"))).thenReturn(true);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                reviewService.createReview(request, "client@test.com")
        );
        assertTrue(ex.getMessage().contains("already submitted"));
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    @DisplayName("createReview: Fails when non-participant attempts to review")
    void testNonParticipantReviewFails() {
        ReviewRequest request = new ReviewRequest(1L, 5, "Unrelated user");
        when(projectRepository.findById(1L)).thenReturn(Optional.of(completedProject));
        when(reviewRepository.existsByProjectIdAndReviewerEmail(any(), eq("intruder@test.com"))).thenReturn(false);
        when(proposalRepository.findByProjectIdAndStatus(any(), eq("ACCEPTED"))).thenReturn(Optional.of(acceptedProposal));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                reviewService.createReview(request, "intruder@test.com")
        );
        assertTrue(ex.getMessage().contains("Only participating client or accepted freelancer"));
    }

    @Test
    @DisplayName("getReviewsForUser: returns reviews mapped to responses")
    void testGetReviewsForUser() {
        Review review = new Review(1L, "client@test.com", "dev@test.com", 5, "Superb");
        when(reviewRepository.findByReviewedUserEmail("dev@test.com")).thenReturn(List.of(review));

        List<ReviewResponse> list = reviewService.getReviewsForUser("dev@test.com");
        assertEquals(1, list.size());
        assertEquals("dev@test.com", list.get(0).getReviewedUserEmail());
    }
}
