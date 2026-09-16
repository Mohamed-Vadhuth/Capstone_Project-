package freelancer_platform.service;

import freelancer_platform.dto.ClientDashboardDto;
import freelancer_platform.dto.ProjectResponseDto;
import freelancer_platform.dto.UserProfileDto;
import freelancer_platform.entity.Project;
import freelancer_platform.exception.ResourceNotFoundException;
import freelancer_platform.repository.ProjectRepository;
import freelancer_platform.repository.ProposalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;

    @Autowired(required = false)
    private ProposalRepository proposalRepository;

    @Autowired(required = false)
    private UserService userService;

    // Preserved 1-arg constructor for existing ProjectServiceTest
    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Autowired
    public ProjectService(ProjectRepository projectRepository,
                          @Autowired(required = false) ProposalRepository proposalRepository,
                          @Autowired(required = false) UserService userService) {
        this.projectRepository = projectRepository;
        this.proposalRepository = proposalRepository;
        this.userService = userService;
    }

    // Create a new project (legacy method signature preserved for tests)
    public Project createProject(Project project) {
        if (project.getStatus() == null || project.getStatus().isBlank()) {
            project.setStatus("OPEN");
        }
        if (project.getCreatedAt() == null) {
            project.setCreatedAt(LocalDateTime.now());
        }
        return projectRepository.save(project);
    }

    // Create a new project with authenticated client email
    public Project createProject(Project project, String clientEmail) {
        if (clientEmail != null && !clientEmail.isBlank()) {
            project.setClientEmail(clientEmail);
        }
        return createProject(project);
    }

    // Get all projects
    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    // Get project by ID
    public Project getProjectById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
    }

    // Search & filter projects with sorting
    public List<ProjectResponseDto> searchProjects(String query, String category,
                                                  Double minBudget, Double maxBudget,
                                                  String status, String sortBy) {
        List<Project> list = projectRepository.searchProjects(
                (query != null && !query.isBlank()) ? query.trim() : null,
                (category != null && !category.isBlank()) ? category.trim() : null,
                (status != null && !status.isBlank() && !status.equalsIgnoreCase("ALL")) ? status.trim() : null,
                minBudget,
                maxBudget
        );

        Comparator<Project> comparator;
        if ("budget_asc".equalsIgnoreCase(sortBy)) {
            comparator = Comparator.comparingDouble(Project::getBudget);
        } else if ("budget_desc".equalsIgnoreCase(sortBy)) {
            comparator = Comparator.comparingDouble(Project::getBudget).reversed();
        } else {
            comparator = Comparator.comparing(
                    p -> p.getCreatedAt() != null ? p.getCreatedAt() : LocalDateTime.MIN,
                    Comparator.reverseOrder()
            );
        }

        return list.stream()
                .sorted(comparator)
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    // Update project status
    public Project updateProjectStatus(Long projectId, String newStatus, String authenticatedEmail) {
        Project project = getProjectById(projectId);

        if (project.getClientEmail() != null && authenticatedEmail != null &&
                !project.getClientEmail().equalsIgnoreCase(authenticatedEmail)) {
            throw new IllegalArgumentException("Only the project owner can update this project status");
        }

        String currentStatus = (project.getStatus() != null) ? project.getStatus().toUpperCase() : "OPEN";
        String targetStatus = (newStatus != null) ? newStatus.toUpperCase().trim() : "";

        if (!List.of("OPEN", "IN_PROGRESS", "COMPLETED").contains(targetStatus)) {
            throw new IllegalArgumentException("Invalid project status: " + newStatus);
        }

        if (currentStatus.equals(targetStatus)) {
            return project;
        }

        if ("COMPLETED".equals(currentStatus)) {
            throw new IllegalStateException("Cannot change status of an already COMPLETED project");
        }

        if ("IN_PROGRESS".equals(currentStatus) && "OPEN".equals(targetStatus)) {
            throw new IllegalStateException("Cannot move an IN_PROGRESS project back to OPEN");
        }

        project.setStatus(targetStatus);
        return projectRepository.save(project);
    }

    // Get client dashboard
    public ClientDashboardDto getClientDashboard(String clientEmail) {
        UserProfileDto profile = (userService != null) ? userService.getProfile(clientEmail) : null;
        List<Project> clientProjects = projectRepository.findByClientEmail(clientEmail);

        int totalProjects = clientProjects.size();
        int openCount = 0;
        int inProgressCount = 0;
        int completedCount = 0;
        int totalProposals = 0;

        List<ProjectResponseDto> dtoList = clientProjects.stream()
                .sorted(Comparator.comparing(
                        p -> p.getCreatedAt() != null ? p.getCreatedAt() : LocalDateTime.MIN,
                        Comparator.reverseOrder()
                ))
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());

        for (ProjectResponseDto p : dtoList) {
            totalProposals += p.getProposalCount();
            String st = (p.getStatus() != null) ? p.getStatus().toUpperCase() : "OPEN";
            if ("OPEN".equals(st)) openCount++;
            else if ("IN_PROGRESS".equals(st)) inProgressCount++;
            else if ("COMPLETED".equals(st)) completedCount++;
        }

        return new ClientDashboardDto(
                profile,
                totalProjects,
                openCount,
                inProgressCount,
                completedCount,
                totalProposals,
                dtoList
        );
    }

    public ProjectResponseDto mapToResponseDto(Project project) {
        int proposalCount = (proposalRepository != null) ? proposalRepository.countByProjectId(project.getId()) : 0;
        return new ProjectResponseDto(
                project.getId(),
                project.getTitle(),
                project.getDescription(),
                project.getBudget(),
                project.getCategory(),
                project.getStatus() != null ? project.getStatus() : "OPEN",
                project.getClientEmail(),
                project.getCreatedAt(),
                proposalCount
        );
    }
}
