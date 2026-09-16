package freelancer_platform.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import freelancer_platform.entity.Project;
import freelancer_platform.security.JwtUtil;
import freelancer_platform.service.ProjectService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @MockitoBean
    private ProjectService projectService;

    @Test
    @DisplayName("GET /projects - public access returns 200 OK with list of projects")
    void testGetAllProjectsPublic() throws Exception {
        Project p1 = new Project("Web App", "React + Spring Boot", 15000, "Fullstack");
        Project p2 = new Project("Mobile App", "Flutter iOS/Android", 20000, "Mobile");

        when(projectService.getAllProjects()).thenReturn(Arrays.asList(p1, p2));

        mockMvc.perform(get("/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("Web App"))
                .andExpect(jsonPath("$[1].title").value("Mobile App"));

        verify(projectService, times(1)).getAllProjects();
    }

    @Test
    @DisplayName("POST /projects - without JWT returns 401 Unauthorized")
    void testCreateProjectWithoutJwt() throws Exception {
        Project project = new Project("New App", "Description", 5000, "Backend");

        mockMvc.perform(post("/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(project)))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(projectService);
    }

    @Test
    @DisplayName("POST /projects - with valid JWT creates project and returns 201 Created")
    void testCreateProjectWithValidJwt() throws Exception {
        String token = jwtUtil.generateToken("client@test.com", "CLIENT");
        Project input = new Project("API Backend", "Build Microservices", 12000, "Backend");
        Project saved = new Project("API Backend", "Build Microservices", 12000, "Backend");

        when(projectService.createProject(any(Project.class))).thenReturn(saved);

        mockMvc.perform(post("/projects")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("API Backend"))
                .andExpect(jsonPath("$.budget").value(12000))
                .andExpect(jsonPath("$.category").value("Backend"));

        verify(projectService, times(1)).createProject(any(Project.class));
    }

    @Test
    @DisplayName("POST /projects - validation failure with blank title, description, category and non-positive budget returns 400 Bad Request")
    void testCreateProjectValidationFailures() throws Exception {
        String token = jwtUtil.generateToken("client@test.com", "CLIENT");
        Project invalidProject = new Project("", "", -500.0, "");

        mockMvc.perform(post("/projects")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidProject)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.title").exists())
                .andExpect(jsonPath("$.errors.description").exists())
                .andExpect(jsonPath("$.errors.budget").exists())
                .andExpect(jsonPath("$.errors.category").exists());

        verifyNoInteractions(projectService);
    }
}
