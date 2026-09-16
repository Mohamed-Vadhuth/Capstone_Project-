package freelancer_platform.controller;

import freelancer_platform.dto.ProjectResponseDto;
import freelancer_platform.entity.Project;
import freelancer_platform.service.ProjectService;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    // Create a new project
    @PostMapping
    public ResponseEntity<Project> createProject(@Valid @RequestBody Project project, Authentication authentication) {
        if (authentication != null && authentication.getName() != null && !authentication.getName().isBlank()) {
            project.setClientEmail(authentication.getName());
        }
        Project savedProject = projectService.createProject(project);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedProject);
    }

    // Get all projects or search with filters
    @GetMapping
    public List<?> getProjects(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Double minBudget,
            @RequestParam(required = false) Double maxBudget,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String sortBy) {

        boolean hasFilters = (query != null && !query.isBlank()) ||
                (category != null && !category.isBlank()) ||
                (status != null && !status.isBlank()) ||
                minBudget != null || maxBudget != null ||
                (sortBy != null && !sortBy.isBlank());

        if (hasFilters) {
            return projectService.searchProjects(query, category, minBudget, maxBudget, status, sortBy);
        }

        return projectService.getAllProjects();
    }

    // Get project by ID
    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponseDto> getProjectById(@PathVariable Long id) {
        Project project = projectService.getProjectById(id);
        return ResponseEntity.ok(projectService.mapToResponseDto(project));
    }

    // Update project status (e.g., mark COMPLETED)
    @PutMapping("/{id}/status")
    public ResponseEntity<Project> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> statusBody,
            Authentication authentication) {
        String newStatus = statusBody.get("status");
        String email = (authentication != null) ? authentication.getName() : null;
        Project updated = projectService.updateProjectStatus(id, newStatus, email);
        return ResponseEntity.ok(updated);
    }
}