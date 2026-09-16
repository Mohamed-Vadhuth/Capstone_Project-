package freelancer_platform.controller;

import freelancer_platform.dto.ClientDashboardDto;
import freelancer_platform.dto.FreelancerDashboardDto;
import freelancer_platform.dto.UserProfileDto;
import freelancer_platform.security.JwtUtil;
import freelancer_platform.service.ProjectService;
import freelancer_platform.service.ProposalService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @MockitoBean
    private ProjectService projectService;

    @MockitoBean
    private ProposalService proposalService;

    @Test
    @DisplayName("GET /dashboard/client - without JWT returns 401 Unauthorized")
    void testClientDashboardWithoutJwt() throws Exception {
        mockMvc.perform(get("/dashboard/client"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /dashboard/client - with valid JWT returns client metrics")
    void testClientDashboardWithJwt() throws Exception {
        String token = jwtUtil.generateToken("client@test.com", "CLIENT");
        UserProfileDto profile = new UserProfileDto(1L, "Client", "client@test.com", "CLIENT", null, null, null, null, 5.0, 1);
        ClientDashboardDto dto = new ClientDashboardDto(profile, 5, 3, 1, 1, 12, Collections.emptyList());

        when(projectService.getClientDashboard("client@test.com")).thenReturn(dto);

        mockMvc.perform(get("/dashboard/client")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProjects").value(5))
                .andExpect(jsonPath("$.openProjects").value(3))
                .andExpect(jsonPath("$.totalProposalsReceived").value(12));
    }

    @Test
    @DisplayName("GET /dashboard/freelancer - with valid JWT returns freelancer metrics")
    void testFreelancerDashboardWithJwt() throws Exception {
        String token = jwtUtil.generateToken("freelancer@test.com", "FREELANCER");
        UserProfileDto profile = new UserProfileDto(2L, "Freelancer", "freelancer@test.com", "FREELANCER", null, null, null, null, 4.8, 4);
        FreelancerDashboardDto dto = new FreelancerDashboardDto(profile, 8, 4, 3, 1, 2, Collections.emptyList());

        when(proposalService.getFreelancerDashboard("freelancer@test.com")).thenReturn(dto);

        mockMvc.perform(get("/dashboard/freelancer")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProposals").value(8))
                .andExpect(jsonPath("$.acceptedProposals").value(3))
                .andExpect(jsonPath("$.completedProjects").value(2));
    }
}
