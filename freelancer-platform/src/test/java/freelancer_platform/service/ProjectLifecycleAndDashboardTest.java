package freelancer_platform.service;

import freelancer_platform.dto.ClientDashboardDto;
import freelancer_platform.dto.ProjectResponseDto;
import freelancer_platform.dto.UserProfileDto;
import freelancer_platform.entity.Project;
import freelancer_platform.repository.ProjectRepository;
import freelancer_platform.repository.ProposalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProjectLifecycleAndDashboardTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProposalRepository proposalRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private ProjectService projectService;

    private Project sampleProject;

    @BeforeEach
    void setUp() {
        sampleProject = new Project("Ecommerce API", "Microservices with Spring", 10000, "Backend", "client@test.com");
        sampleProject.setStatus("OPEN");
        sampleProject.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("updateProjectStatus: successfully transitions from OPEN to IN_PROGRESS")
    void testTransitionOpenToInProgress() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(sampleProject));
        when(projectRepository.save(any(Project.class))).thenAnswer(i -> i.getArguments()[0]);

        Project updated = projectService.updateProjectStatus(1L, "IN_PROGRESS", "client@test.com");
        assertEquals("IN_PROGRESS", updated.getStatus());
        verify(projectRepository, times(1)).save(sampleProject);
    }

    @Test
    @DisplayName("updateProjectStatus: successfully transitions from IN_PROGRESS to COMPLETED")
    void testTransitionInProgressToCompleted() {
        sampleProject.setStatus("IN_PROGRESS");
        when(projectRepository.findById(1L)).thenReturn(Optional.of(sampleProject));
        when(projectRepository.save(any(Project.class))).thenAnswer(i -> i.getArguments()[0]);

        Project updated = projectService.updateProjectStatus(1L, "COMPLETED", "client@test.com");
        assertEquals("COMPLETED", updated.getStatus());
    }

    @Test
    @DisplayName("updateProjectStatus: rejects transition if not owner")
    void testTransitionByNonOwnerFails() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(sampleProject));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                projectService.updateProjectStatus(1L, "IN_PROGRESS", "other@test.com")
        );
        assertTrue(ex.getMessage().contains("Only the project owner"));
    }

    @Test
    @DisplayName("updateProjectStatus: rejects transition from COMPLETED")
    void testTransitionFromCompletedFails() {
        sampleProject.setStatus("COMPLETED");
        when(projectRepository.findById(1L)).thenReturn(Optional.of(sampleProject));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                projectService.updateProjectStatus(1L, "OPEN", "client@test.com")
        );
        assertTrue(ex.getMessage().contains("Cannot change status of an already COMPLETED"));
    }

    @Test
    @DisplayName("updateProjectStatus: rejects invalid status value")
    void testInvalidStatusValueFails() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(sampleProject));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                projectService.updateProjectStatus(1L, "DESTROYED", "client@test.com")
        );
        assertTrue(ex.getMessage().contains("Invalid project status"));
    }

    @Test
    @DisplayName("searchProjects: filters and sorts correctly")
    void testSearchProjectsSorting() {
        Project p2 = new Project("Backend API", "Spring Boot API", 15000, "Backend");
        when(projectRepository.searchProjects("API", null, null, null, null)).thenReturn(List.of(p2));

        List<ProjectResponseDto> results = projectService.searchProjects("API", null, null, null, null, "budget_desc");
        assertEquals(1, results.size());
        assertEquals("Backend API", results.get(0).getTitle());
    }

    @Test
    @DisplayName("getClientDashboard: aggregates metrics accurately")
    void testGetClientDashboardMetrics() {
        Project p1 = new Project("App 1", "Desc 1", 5000, "Web", "client@test.com");
        p1.setStatus("OPEN");
        Project p2 = new Project("App 2", "Desc 2", 15000, "Web", "client@test.com");
        p2.setStatus("COMPLETED");

        when(projectRepository.findByClientEmail("client@test.com")).thenReturn(List.of(p1, p2));
        when(proposalRepository.countByProjectId(any())).thenReturn(3);
        when(userService.getProfile("client@test.com")).thenReturn(new UserProfileDto(1L, "Client", "client@test.com", "CLIENT", null, null, null, null, 4.5, 2));

        ClientDashboardDto dashboard = projectService.getClientDashboard("client@test.com");

        assertNotNull(dashboard);
        assertEquals(2, dashboard.getTotalProjects());
        assertEquals(1, dashboard.getOpenProjects());
        assertEquals(1, dashboard.getCompletedProjects());
        assertEquals(6, dashboard.getTotalProposalsReceived());
        assertEquals(2, dashboard.getProjects().size());
    }
}
