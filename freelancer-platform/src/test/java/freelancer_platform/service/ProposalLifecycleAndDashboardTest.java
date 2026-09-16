package freelancer_platform.service;

import freelancer_platform.dto.FreelancerDashboardDto;
import freelancer_platform.dto.UserProfileDto;
import freelancer_platform.entity.Project;
import freelancer_platform.entity.Proposal;
import freelancer_platform.repository.ProjectRepository;
import freelancer_platform.repository.ProposalRepository;
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
public class ProposalLifecycleAndDashboardTest {

    @Mock
    private ProposalRepository proposalRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private ProposalService proposalService;

    private Proposal sampleProposal;
    private Project sampleProject;

    @BeforeEach
    void setUp() {
        sampleProposal = new Proposal(10L, "freelancer@test.com", "Expert developer", 2500.0, "1 week");
        sampleProposal.setStatus("PENDING");

        sampleProject = new Project("Mobile App", "iOS App", 5000.0, "Mobile", "client@test.com");
        sampleProject.setStatus("OPEN");
    }

    @Test
    @DisplayName("acceptProposal: fails if proposal is already ACCEPTED")
    void testAcceptAlreadyAcceptedFails() {
        sampleProposal.setStatus("ACCEPTED");
        when(proposalRepository.findById(1L)).thenReturn(Optional.of(sampleProposal));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                proposalService.acceptProposal(1L, "client@test.com")
        );
        assertTrue(ex.getMessage().contains("already ACCEPTED"));
    }

    @Test
    @DisplayName("acceptProposal: fails if proposal is already REJECTED")
    void testAcceptAlreadyRejectedFails() {
        sampleProposal.setStatus("REJECTED");
        when(proposalRepository.findById(1L)).thenReturn(Optional.of(sampleProposal));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                proposalService.acceptProposal(1L, "client@test.com")
        );
        assertTrue(ex.getMessage().contains("Cannot accept a REJECTED proposal"));
    }

    @Test
    @DisplayName("rejectProposal: fails if proposal is already ACCEPTED")
    void testRejectAlreadyAcceptedFails() {
        sampleProposal.setStatus("ACCEPTED");
        when(proposalRepository.findById(1L)).thenReturn(Optional.of(sampleProposal));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                proposalService.rejectProposal(1L, "client@test.com")
        );
        assertTrue(ex.getMessage().contains("Cannot reject an already ACCEPTED proposal"));
    }

    @Test
    @DisplayName("createProposal: fails if project is COMPLETED")
    void testCreateProposalOnCompletedProjectFails() {
        sampleProject.setStatus("COMPLETED");
        when(projectRepository.findById(10L)).thenReturn(Optional.of(sampleProject));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                proposalService.createProposal(sampleProposal)
        );
        assertTrue(ex.getMessage().contains("Cannot submit proposals to a completed project"));
    }

    @Test
    @DisplayName("acceptProposal: updates project status to IN_PROGRESS")
    void testAcceptProposalMovesProjectToInProgress() {
        when(proposalRepository.findById(1L)).thenReturn(Optional.of(sampleProposal));
        when(projectRepository.findById(10L)).thenReturn(Optional.of(sampleProject));
        when(proposalRepository.save(any(Proposal.class))).thenAnswer(i -> i.getArguments()[0]);

        Proposal accepted = proposalService.acceptProposal(1L, "client@test.com");
        assertEquals("ACCEPTED", accepted.getStatus());
        assertEquals("IN_PROGRESS", sampleProject.getStatus());
        verify(projectRepository, times(1)).save(sampleProject);
    }

    @Test
    @DisplayName("getFreelancerDashboard: calculates proposal metrics accurately")
    void testFreelancerDashboardMetrics() {
        Proposal p1 = new Proposal(1L, "freelancer@test.com", "Letter 1", 1000);
        p1.setStatus("PENDING");
        Proposal p2 = new Proposal(2L, "freelancer@test.com", "Letter 2", 2000);
        p2.setStatus("ACCEPTED");

        when(proposalRepository.findByFreelancerEmail("freelancer@test.com")).thenReturn(List.of(p1, p2));
        when(userService.getProfile("freelancer@test.com")).thenReturn(new UserProfileDto(2L, "Free", "freelancer@test.com", "FREELANCER", null, null, null, null, 5.0, 1));
        when(projectRepository.findById(any())).thenReturn(Optional.of(sampleProject));

        FreelancerDashboardDto dashboard = proposalService.getFreelancerDashboard("freelancer@test.com");

        assertNotNull(dashboard);
        assertEquals(2, dashboard.getTotalProposals());
        assertEquals(1, dashboard.getPendingProposals());
        assertEquals(1, dashboard.getAcceptedProposals());
        assertEquals(2, dashboard.getProposals().size());
    }
}
