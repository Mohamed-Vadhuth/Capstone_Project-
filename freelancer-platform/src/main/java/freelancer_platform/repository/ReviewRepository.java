package freelancer_platform.repository;

import freelancer_platform.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByProjectId(Long projectId);

    List<Review> findByReviewedUserEmail(String reviewedUserEmail);

    Optional<Review> findByProjectIdAndReviewerEmail(Long projectId, String reviewerEmail);

    boolean existsByProjectIdAndReviewerEmail(Long projectId, String reviewerEmail);
}
