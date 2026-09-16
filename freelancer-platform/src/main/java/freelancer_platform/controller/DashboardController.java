package freelancer_platform.controller;

import freelancer_platform.dto.ClientDashboardDto;
import freelancer_platform.dto.FreelancerDashboardDto;
import freelancer_platform.service.ProjectService;
import freelancer_platform.service.ProposalService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    private final ProjectService projectService;
    private final ProposalService proposalService;

    public DashboardController(ProjectService projectService, ProposalService proposalService) {
        this.projectService = projectService;
        this.proposalService = proposalService;
    }

    @GetMapping("/client")
    public ResponseEntity<ClientDashboardDto> getClientDashboard(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        ClientDashboardDto dashboard = projectService.getClientDashboard(principal.getName());
        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/freelancer")
    public ResponseEntity<FreelancerDashboardDto> getFreelancerDashboard(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        FreelancerDashboardDto dashboard = proposalService.getFreelancerDashboard(principal.getName());
        return ResponseEntity.ok(dashboard);
    }
}
