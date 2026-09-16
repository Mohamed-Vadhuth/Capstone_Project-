package freelancer_platform.service;

import freelancer_platform.dto.ReviewRequest;
import freelancer_platform.dto.ReviewResponse;
import freelancer_platform.entity.Project;
import freelancer_platform.entity.Proposal;
import freelancer_platform.entity.Review;
import freelancer_platform.entity.User;
import freelancer_platform.exception.ResourceNotFoundException;
import freelancer_platform.repository.ProjectRepository;
import freelancer_platform.repository.ProposalRepository;
import freelancer_platform.repository.ReviewRepository;
import freelancer_platform.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProjectRepository projectRepository;
    private final ProposalRepository proposalRepository;
    private final UserRepository userRepository;

    public ReviewService(ReviewRepository reviewRepository,
                         ProjectRepository projectRepository,
                         ProposalRepository proposalRepository,
                         UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.projectRepository = projectRepository;
        this.proposalRepository = proposalRepository;
        this.userRepository = userRepository;
    }

    public ReviewResponse createReview(ReviewRequest request, String reviewerEmail) {
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + request.getProjectId()));

        // Reviews can only be submitted for completed projects
        if (!"COMPLETED".equalsIgnoreCase(project.getStatus())) {
            throw new IllegalStateException("Reviews can only be submitted for COMPLETED projects");
        }

        // Check if reviewer has already reviewed this project
        if (reviewRepository.existsByProjectIdAndReviewerEmail(project.getId(), reviewerEmail)) {
            throw new IllegalStateException("You have already submitted a review for this project");
        }

        // Identify participants: Client and Accepted Freelancer
        String clientEmail = project.getClientEmail();
        Optional<Proposal> acceptedProposalOpt = proposalRepository.findByProjectIdAndStatus(project.getId(), "ACCEPTED");
        String acceptedFreelancerEmail = acceptedProposalOpt.map(Proposal::getFreelancerEmail).orElse(null);

        String targetReviewedEmail;

        if (reviewerEmail.equalsIgnoreCase(clientEmail)) {
            // Reviewer is client -> reviewing accepted freelancer
            if (acceptedFreelancerEmail == null) {
                throw new IllegalStateException("No accepted freelancer found to review for this project");
            }
            targetReviewedEmail = acceptedFreelancerEmail;
        } else if (acceptedFreelancerEmail != null && reviewerEmail.equalsIgnoreCase(acceptedFreelancerEmail)) {
            // Reviewer is freelancer -> reviewing client
            if (clientEmail == null) {
                throw new IllegalStateException("Project has no registered client owner to review");
            }
            targetReviewedEmail = clientEmail;
        } else {
            throw new IllegalArgumentException("Only participating client or accepted freelancer of this completed project can submit a review");
        }

        if (reviewerEmail.equalsIgnoreCase(targetReviewedEmail)) {
            throw new IllegalArgumentException("Users cannot review themselves");
        }

        Review review = new Review(
                project.getId(),
                reviewerEmail,
                targetReviewedEmail,
                request.getRating(),
                request.getComment()
        );

        Review saved = reviewRepository.save(review);
        return mapToResponse(saved);
    }

    public List<ReviewResponse> getReviewsForUser(String email) {
        return reviewRepository.findByReviewedUserEmail(email).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<ReviewResponse> getReviewsForProject(Long projectId) {
        return reviewRepository.findByProjectId(projectId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private ReviewResponse mapToResponse(Review r) {
        String reviewerName = r.getReviewerEmail();
        Optional<User> userOpt = userRepository.findByEmail(r.getReviewerEmail());
        if (userOpt.isPresent()) {
            reviewerName = userOpt.get().getName();
        }

        return new ReviewResponse(
                r.getId(),
                r.getProjectId(),
                r.getReviewerEmail(),
                reviewerName,
                r.getReviewedUserEmail(),
                r.getRating(),
                r.getComment(),
                r.getCreatedAt()
        );
    }
}
