package freelancer_platform.controller;

import freelancer_platform.entity.Proposal;
import freelancer_platform.service.ProposalService;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/proposals")
public class ProposalController {

    private final ProposalService proposalService;

    public ProposalController(ProposalService proposalService) {
        this.proposalService = proposalService;
    }

    // Submit a proposal
    @PostMapping
    public ResponseEntity<Proposal> createProposal(@Valid @RequestBody Proposal proposal, Authentication authentication) {
        if (authentication != null && authentication.getName() != null && !authentication.getName().isBlank()) {
            if (proposal.getFreelancerEmail() == null || proposal.getFreelancerEmail().isBlank()) {
                proposal.setFreelancerEmail(authentication.getName());
            }
        }
        Proposal savedProposal = proposalService.createProposal(proposal);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedProposal);
    }

    // View all proposals
    @GetMapping
    public List<Proposal> getAllProposals() {
        return proposalService.getAllProposals();
    }

    // View proposals for a specific project
    @GetMapping("/project/{projectId}")
    public List<Proposal> getProposalsByProject(
            @PathVariable Long projectId) {
        return proposalService.getProposalsByProject(projectId);
    }

    // Accept a proposal
    @PutMapping("/{id}/accept")
    public Proposal acceptProposal(@PathVariable Long id) {
        return proposalService.acceptProposal(id);
    }

    // Reject a proposal
    @PutMapping("/{id}/reject")
    public Proposal rejectProposal(@PathVariable Long id) {
        return proposalService.rejectProposal(id);
    }
}