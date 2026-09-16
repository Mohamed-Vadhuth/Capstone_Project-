package freelancer_platform.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import freelancer_platform.entity.Proposal;
import freelancer_platform.exception.ResourceNotFoundException;
import freelancer_platform.security.JwtUtil;
import freelancer_platform.service.ProposalService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ProposalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @MockitoBean
    private ProposalService proposalService;

    @Test
    @DisplayName("GET /proposals - public endpoint returns all proposals")
    void testGetAllProposalsPublic() throws Exception {
        Proposal prop = new Proposal(1L, "dev@test.com", "Ready to start immediately.", 1500.0);
        when(proposalService.getAllProposals()).thenReturn(List.of(prop));

        mockMvc.perform(get("/proposals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].freelancerEmail").value("dev@test.com"));

        verify(proposalService, times(1)).getAllProposals();
    }

    @Test
    @DisplayName("GET /proposals/project/{projectId} - public endpoint returns proposals for project")
    void testGetProposalsByProjectPublic() throws Exception {
        Proposal prop = new Proposal(5L, "coder@test.com", "High quality work guaranteed.", 3000.0);
        when(proposalService.getProposalsByProject(5L)).thenReturn(List.of(prop));

        mockMvc.perform(get("/proposals/project/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].projectId").value(5))
                .andExpect(jsonPath("$[0].freelancerEmail").value("coder@test.com"));

        verify(proposalService, times(1)).getProposalsByProject(5L);
    }

    @Test
    @DisplayName("POST /proposals - without JWT returns 401 Unauthorized")
    void testCreateProposalWithoutJwt() throws Exception {
        Proposal prop = new Proposal(1L, "dev@test.com", "Cover letter text", 1000.0);

        mockMvc.perform(post("/proposals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(prop)))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(proposalService);
    }

    @Test
    @DisplayName("POST /proposals - with valid JWT creates proposal and returns 201 Created")
    void testCreateProposalWithValidJwt() throws Exception {
        String token = jwtUtil.generateToken("dev@test.com", "FREELANCER");
        Proposal input = new Proposal(1L, "dev@test.com", "Experienced developer proposal", 2000.0);
        Proposal saved = new Proposal(1L, "dev@test.com", "Experienced developer proposal", 2000.0);

        when(proposalService.createProposal(any(Proposal.class))).thenReturn(saved);

        mockMvc.perform(post("/proposals")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.projectId").value(1))
                .andExpect(jsonPath("$.freelancerEmail").value("dev@test.com"))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(proposalService, times(1)).createProposal(any(Proposal.class));
    }

    @Test
    @DisplayName("POST /proposals - validation failure with missing projectId, invalid email, blank cover letter, negative amount returns 400 Bad Request")
    void testCreateProposalValidationFailure() throws Exception {
        String token = jwtUtil.generateToken("dev@test.com", "FREELANCER");
        Proposal invalidProposal = new Proposal(null, "invalid-email", "", -100.0);

        mockMvc.perform(post("/proposals")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidProposal)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.projectId").exists())
                .andExpect(jsonPath("$.errors.freelancerEmail").exists())
                .andExpect(jsonPath("$.errors.coverLetter").exists())
                .andExpect(jsonPath("$.errors.proposedAmount").exists());

        verifyNoInteractions(proposalService);
    }

    @Test
    @DisplayName("PUT /proposals/{id}/accept - without JWT returns 401 Unauthorized")
    void testAcceptProposalWithoutJwt() throws Exception {
        mockMvc.perform(put("/proposals/1/accept"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(proposalService);
    }

    @Test
    @DisplayName("PUT /proposals/{id}/accept - with valid JWT accepts proposal and returns 200 OK")
    void testAcceptProposalWithValidJwt() throws Exception {
        String token = jwtUtil.generateToken("client@test.com", "CLIENT");
        Proposal accepted = new Proposal(1L, "dev@test.com", "Cover letter", 1500.0);
        accepted.setStatus("ACCEPTED");

        when(proposalService.acceptProposal(1L)).thenReturn(accepted);

        mockMvc.perform(put("/proposals/1/accept")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));

        verify(proposalService, times(1)).acceptProposal(1L);
    }

    @Test
    @DisplayName("PUT /proposals/{id}/accept - nonexistent proposal returns 404 Not Found")
    void testAcceptProposalNotFound() throws Exception {
        String token = jwtUtil.generateToken("client@test.com", "CLIENT");
        when(proposalService.acceptProposal(999L))
                .thenThrow(new ResourceNotFoundException("Proposal not found with id: 999"));

        mockMvc.perform(put("/proposals/999/accept")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Proposal not found with id: 999"));

        verify(proposalService, times(1)).acceptProposal(999L);
    }

    @Test
    @DisplayName("PUT /proposals/{id}/reject - without JWT returns 401 Unauthorized")
    void testRejectProposalWithoutJwt() throws Exception {
        mockMvc.perform(put("/proposals/1/reject"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(proposalService);
    }

    @Test
    @DisplayName("PUT /proposals/{id}/reject - with valid JWT rejects proposal and returns 200 OK")
    void testRejectProposalWithValidJwt() throws Exception {
        String token = jwtUtil.generateToken("client@test.com", "CLIENT");
        Proposal rejected = new Proposal(1L, "dev@test.com", "Cover letter", 1500.0);
        rejected.setStatus("REJECTED");

        when(proposalService.rejectProposal(1L)).thenReturn(rejected);

        mockMvc.perform(put("/proposals/1/reject")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));

        verify(proposalService, times(1)).rejectProposal(1L);
    }

    @Test
    @DisplayName("PUT /proposals/{id}/reject - nonexistent proposal returns 404 Not Found")
    void testRejectProposalNotFound() throws Exception {
        String token = jwtUtil.generateToken("client@test.com", "CLIENT");
        when(proposalService.rejectProposal(999L))
                .thenThrow(new ResourceNotFoundException("Proposal not found with id: 999"));

        mockMvc.perform(put("/proposals/999/reject")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Proposal not found with id: 999"));

        verify(proposalService, times(1)).rejectProposal(999L);
    }
}
