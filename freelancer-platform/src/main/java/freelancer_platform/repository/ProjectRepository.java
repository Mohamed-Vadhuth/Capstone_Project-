package freelancer_platform.repository;

import freelancer_platform.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByClientEmail(String clientEmail);

    List<Project> findByStatus(String status);

    int countByClientEmail(String clientEmail);

    int countByClientEmailAndStatus(String clientEmail, String status);

    @Query("SELECT p FROM Project p WHERE " +
            "(:query IS NULL OR :query = '' OR LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
            "(:category IS NULL OR :category = '' OR LOWER(p.category) = LOWER(:category)) AND " +
            "(:status IS NULL OR :status = '' OR UPPER(p.status) = UPPER(:status)) AND " +
            "(:minBudget IS NULL OR p.budget >= :minBudget) AND " +
            "(:maxBudget IS NULL OR p.budget <= :maxBudget)")
    List<Project> searchProjects(@Param("query") String query,
                                 @Param("category") String category,
                                 @Param("status") String status,
                                 @Param("minBudget") Double minBudget,
                                 @Param("maxBudget") Double maxBudget);
}