package freelancer_platform.service;

import freelancer_platform.entity.Project;
import freelancer_platform.repository.ProjectRepository;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private ProjectService projectService;

    private Project sampleProject;

    @BeforeEach
    void setUp() {
        sampleProject = new Project("Build REST API", "Design and build Spring Boot REST API", 5000.0, "Backend");
    }

    @Test
    @DisplayName("createProject: successfully saves and returns the project")
    void testCreateProjectSuccess() {
        // Arrange
        when(projectRepository.save(any(Project.class))).thenReturn(sampleProject);

        // Act
        Project result = projectService.createProject(sampleProject);

        // Assert
        assertNotNull(result);
        assertEquals("Build REST API", result.getTitle());
        assertEquals("Design and build Spring Boot REST API", result.getDescription());
        assertEquals(5000.0, result.getBudget());
        assertEquals("Backend", result.getCategory());

        verify(projectRepository, times(1)).save(sampleProject);
    }

    @Test
    @DisplayName("getAllProjects: returns list of all projects from repository")
    void testGetAllProjects() {
        // Arrange
        Project p2 = new Project("Mobile App UI", "React Native UI implementation", 8000.0, "Mobile");
        List<Project> projectList = Arrays.asList(sampleProject, p2);
        when(projectRepository.findAll()).thenReturn(projectList);

        // Act
        List<Project> result = projectService.getAllProjects();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Build REST API", result.get(0).getTitle());
        assertEquals("Mobile App UI", result.get(1).getTitle());

        verify(projectRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getAllProjects: returns empty list when no projects exist")
    void testGetAllProjectsEmpty() {
        // Arrange
        when(projectRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<Project> result = projectService.getAllProjects();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(projectRepository, times(1)).findAll();
    }
}
