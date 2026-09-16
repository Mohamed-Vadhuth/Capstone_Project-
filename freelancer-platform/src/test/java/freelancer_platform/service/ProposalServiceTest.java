package freelancer_platform.service;

import freelancer_platform.entity.Proposal;
import freelancer_platform.exception.ResourceNotFoundException;
import freelancer_platform.repository.ProposalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProposalServiceTest {

    @Mock
    private ProposalRepository proposalRepository;

    @InjectMocks
    private ProposalService proposalService;

    private Proposal sampleProposal;

    @BeforeEach
    void setUp() {
        sampleProposal = new Proposal(10L, "freelancer@test.com", "Experienced Java developer with 5+ years experience.", 2500.0);
    }

    @Test
    @DisplayName("createProposal: successfully saves and returns proposal with initial PENDING status")
    void testCreateProposalSuccess() {
        // Arrange
        when(proposalRepository.save(any(Proposal.class))).thenReturn(sampleProposal);

        // Act
        Proposal result = proposalService.createProposal(sampleProposal);

        // Assert
        assertNotNull(result);
        assertEquals(10L, result.getProjectId());
        assertEquals("freelancer@test.com", result.getFreelancerEmail());
        assertEquals("PENDING", result.getStatus());
        assertEquals(2500.0, result.getProposedAmount());

        verify(proposalRepository, times(1)).save(sampleProposal);
    }

    @Test
    @DisplayName("getAllProposals: returns list of all proposals")
    void testGetAllProposals() {
        // Arrange
        Proposal prop2 = new Proposal(20L, "dev2@test.com", "Frontend specialist", 1500.0);
        when(proposalRepository.findAll()).thenReturn(Arrays.asList(sampleProposal, prop2));

        // Act
        List<Proposal> result = proposalService.getAllProposals();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(proposalRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getProposalsByProject: returns proposals matching given projectId")
    void testGetProposalsByProject() {
        // Arrange
        when(proposalRepository.findByProjectId(10L)).thenReturn(Collections.singletonList(sampleProposal));

        // Act
        List<Proposal> result = proposalService.getProposalsByProject(10L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("freelancer@test.com", result.get(0).getFreelancerEmail());
        verify(proposalRepository, times(1)).findByProjectId(10L);
    }

    @Test
    @DisplayName("acceptProposal: updates status to ACCEPTED and saves to repository")
    void testAcceptProposalSuccess() {
        // Arrange
        when(proposalRepository.findById(1L)).thenReturn(Optional.of(sampleProposal));
        when(proposalRepository.save(any(Proposal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Proposal result = proposalService.acceptProposal(1L);

        // Assert
        assertNotNull(result);
        assertEquals("ACCEPTED", result.getStatus());
        verify(proposalRepository, times(1)).findById(1L);
        verify(proposalRepository, times(1)).save(sampleProposal);
    }

    @Test
    @DisplayName("acceptProposal: throws ResourceNotFoundException when proposal does not exist")
    void testAcceptProposalNotFound() {
        // Arrange
        when(proposalRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            proposalService.acceptProposal(999L);
        });

        assertEquals("Proposal not found with id: 999", exception.getMessage());
        verify(proposalRepository, times(1)).findById(999L);
        verify(proposalRepository, never()).save(any());
    }

    @Test
    @DisplayName("rejectProposal: updates status to REJECTED and saves to repository")
    void testRejectProposalSuccess() {
        // Arrange
        when(proposalRepository.findById(1L)).thenReturn(Optional.of(sampleProposal));
        when(proposalRepository.save(any(Proposal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Proposal result = proposalService.rejectProposal(1L);

        // Assert
        assertNotNull(result);
        assertEquals("REJECTED", result.getStatus());
        verify(proposalRepository, times(1)).findById(1L);
        verify(proposalRepository, times(1)).save(sampleProposal);
    }

    @Test
    @DisplayName("rejectProposal: throws ResourceNotFoundException when proposal does not exist")
    void testRejectProposalNotFound() {
        // Arrange
        when(proposalRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            proposalService.rejectProposal(999L);
        });

        assertEquals("Proposal not found with id: 999", exception.getMessage());
        verify(proposalRepository, times(1)).findById(999L);
        verify(proposalRepository, never()).save(any());
    }
}
