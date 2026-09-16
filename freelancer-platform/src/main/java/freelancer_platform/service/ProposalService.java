package freelancer_platform.service;

import freelancer_platform.dto.FreelancerDashboardDto;
import freelancer_platform.dto.ProposalResponseDto;
import freelancer_platform.dto.UserProfileDto;
import freelancer_platform.entity.Project;
import freelancer_platform.entity.Proposal;
import freelancer_platform.entity.User;
import freelancer_platform.exception.ResourceNotFoundException;
import freelancer_platform.repository.ProjectRepository;
import freelancer_platform.repository.ProposalRepository;
import freelancer_platform.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProposalService {

    private final ProposalRepository proposalRepository;

    @Autowired(required = false)
    private ProjectRepository projectRepository;

    @Autowired(required = false)
    private UserRepository userRepository;

    @Autowired(required = false)
    private UserService userService;

    // Preserved 1-arg constructor for existing ProposalServiceTest
    public ProposalService(ProposalRepository proposalRepository) {
        this.proposalRepository = proposalRepository;
    }

    @Autowired
    public ProposalService(ProposalRepository proposalRepository,
                           @Autowired(required = false) ProjectRepository projectRepository,
                           @Autowired(required = false) UserRepository userRepository,
                           @Autowired(required = false) UserService userService) {
        this.proposalRepository = proposalRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    // Submit a proposal (legacy overload for existing tests)
    public Proposal createProposal(Proposal proposal) {
        validateProjectAllowsProposals(proposal.getProjectId());
        if (proposal.getStatus() == null || proposal.getStatus().isBlank()) {
            proposal.setStatus("PENDING");
        }
        if (proposal.getCreatedAt() == null) {
            proposal.setCreatedAt(LocalDateTime.now());
        }
        return proposalRepository.save(proposal);
    }

    // Submit a proposal with authenticated freelancer identity
    public Proposal createProposal(Proposal proposal, String authenticatedEmail) {
        if (authenticatedEmail != null && !authenticatedEmail.isBlank()) {
            proposal.setFreelancerEmail(authenticatedEmail);
        }
        return createProposal(proposal);
    }

    private void validateProjectAllowsProposals(Long projectId) {
        if (projectId != null && projectRepository != null) {
            Optional<Project> projectOpt = projectRepository.findById(projectId);
            if (projectOpt.isPresent()) {
                Project p = projectOpt.get();
                if ("COMPLETED".equalsIgnoreCase(p.getStatus())) {
                    throw new IllegalStateException("Cannot submit proposals to a completed project");
                }
            }
        }
    }

    // View all proposals
    public List<Proposal> getAllProposals() {
        return proposalRepository.findAll();
    }

    // View proposals for a specific project
    public List<Proposal> getProposalsByProject(Long projectId) {
        return proposalRepository.findByProjectId(projectId);
    }

    // Accept a proposal (legacy signature preserved for existing tests)
    public Proposal acceptProposal(Long id) {
        return acceptProposal(id, null);
    }

    // Accept a proposal with owner validation and lifecycle transition
    public Proposal acceptProposal(Long id, String clientEmail) {
        Proposal proposal = proposalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proposal not found with id: " + id));

        String currentStatus = (proposal.getStatus() != null) ? proposal.getStatus().toUpperCase() : "PENDING";
        if ("ACCEPTED".equals(currentStatus)) {
            throw new IllegalStateException("Proposal is already ACCEPTED");
        }
        if ("REJECTED".equals(currentStatus)) {
            throw new IllegalStateException("Cannot accept a REJECTED proposal");
        }

        // Validate project ownership if projectRepository and clientEmail are present
        if (projectRepository != null && proposal.getProjectId() != null) {
            Project project = projectRepository.findById(proposal.getProjectId()).orElse(null);
            if (project != null) {
                if (clientEmail != null && project.getClientEmail() != null &&
                        !project.getClientEmail().equalsIgnoreCase(clientEmail)) {
                    throw new IllegalArgumentException("Only the project owner can accept proposals for this project");
                }
                if ("COMPLETED".equalsIgnoreCase(project.getStatus())) {
                    throw new IllegalStateException("Cannot accept proposals for a COMPLETED project");
                }
                project.setStatus("IN_PROGRESS");
                projectRepository.save(project);
            }
        }

        proposal.setStatus("ACCEPTED");
        return proposalRepository.save(proposal);
    }

    // Reject a proposal (legacy signature preserved for existing tests)
    public Proposal rejectProposal(Long id) {
        return rejectProposal(id, null);
    }

    // Reject a proposal with lifecycle validation
    public Proposal rejectProposal(Long id, String clientEmail) {
        Proposal proposal = proposalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proposal not found with id: " + id));

        String currentStatus = (proposal.getStatus() != null) ? proposal.getStatus().toUpperCase() : "PENDING";
        if ("REJECTED".equals(currentStatus)) {
            throw new IllegalStateException("Proposal is already REJECTED");
        }
        if ("ACCEPTED".equals(currentStatus)) {
            throw new IllegalStateException("Cannot reject an already ACCEPTED proposal");
        }

        if (projectRepository != null && proposal.getProjectId() != null && clientEmail != null) {
            Project project = projectRepository.findById(proposal.getProjectId()).orElse(null);
            if (project != null && project.getClientEmail() != null &&
                    !project.getClientEmail().equalsIgnoreCase(clientEmail)) {
                throw new IllegalArgumentException("Only the project owner can reject proposals for this project");
            }
        }

        proposal.setStatus("REJECTED");
        return proposalRepository.save(proposal);
    }

    // Get freelancer dashboard data
    public FreelancerDashboardDto getFreelancerDashboard(String freelancerEmail) {
        UserProfileDto profile = (userService != null) ? userService.getProfile(freelancerEmail) : null;
        List<Proposal> proposalList = proposalRepository.findByFreelancerEmail(freelancerEmail);

        int totalProposals = proposalList.size();
        int pending = 0;
        int accepted = 0;
        int rejected = 0;
        int completedProjectsCount = 0;

        List<ProposalResponseDto> dtoList = proposalList.stream()
                .sorted(Comparator.comparing(
                        p -> p.getCreatedAt() != null ? p.getCreatedAt() : LocalDateTime.MIN,
                        Comparator.reverseOrder()
                ))
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());

        for (ProposalResponseDto p : dtoList) {
            String st = (p.getStatus() != null) ? p.getStatus().toUpperCase() : "PENDING";
            if ("ACCEPTED".equals(st)) {
                accepted++;
                if ("COMPLETED".equalsIgnoreCase(p.getProjectStatus())) {
                    completedProjectsCount++;
                }
            } else if ("REJECTED".equals(st)) {
                rejected++;
            } else {
                pending++;
            }
        }

        return new FreelancerDashboardDto(
                profile,
                totalProposals,
                pending,
                accepted,
                rejected,
                completedProjectsCount,
                dtoList
        );
    }

    public ProposalResponseDto mapToResponseDto(Proposal p) {
        String projectTitle = "Project #" + p.getProjectId();
        String projectStatus = "UNKNOWN";

        if (projectRepository != null && p.getProjectId() != null) {
            Optional<Project> projectOpt = projectRepository.findById(p.getProjectId());
            if (projectOpt.isPresent()) {
                projectTitle = projectOpt.get().getTitle();
                projectStatus = projectOpt.get().getStatus() != null ? projectOpt.get().getStatus() : "OPEN";
            }
        }

        String freelancerName = p.getFreelancerEmail();
        if (userRepository != null && p.getFreelancerEmail() != null) {
            Optional<User> userOpt = userRepository.findByEmail(p.getFreelancerEmail());
            if (userOpt.isPresent()) {
                freelancerName = userOpt.get().getName();
            }
        }

        return new ProposalResponseDto(
                p.getId(),
                p.getProjectId(),
                projectTitle,
                projectStatus,
                p.getFreelancerEmail(),
                freelancerName,
                p.getCoverLetter(),
                p.getProposedAmount(),
                p.getStatus(),
                p.getEstimatedDelivery(),
                p.getCreatedAt()
        );
    }
}
