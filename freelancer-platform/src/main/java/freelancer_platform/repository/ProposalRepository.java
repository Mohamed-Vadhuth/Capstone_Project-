package freelancer_platform.repository;

import freelancer_platform.entity.Proposal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProposalRepository extends JpaRepository<Proposal, Long> {

    List<Proposal> findByProjectId(Long projectId);

    List<Proposal> findByFreelancerEmail(String freelancerEmail);

    int countByProjectId(Long projectId);

    int countByFreelancerEmail(String freelancerEmail);

    int countByFreelancerEmailAndStatus(String freelancerEmail, String status);

    Optional<Proposal> findByProjectIdAndStatus(Long projectId, String status);
}