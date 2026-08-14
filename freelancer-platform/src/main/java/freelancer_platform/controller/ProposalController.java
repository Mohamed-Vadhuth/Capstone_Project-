package freelancer_platform.controller;

import freelancer_platform.entity.Proposal;
import freelancer_platform.repository.ProposalRepository;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/proposals")
public class ProposalController {

    private final ProposalRepository proposalRepository;

    public ProposalController(ProposalRepository proposalRepository) {
        this.proposalRepository = proposalRepository;
    }

    // Submit a proposal
    @PostMapping
    public Proposal createProposal(@RequestBody Proposal proposal) {
        return proposalRepository.save(proposal);
    }

    // View all proposals
    @GetMapping
    public List<Proposal> getAllProposals() {
        return proposalRepository.findAll();
    }

    // View proposals for a specific project
    @GetMapping("/project/{projectId}")
    public List<Proposal> getProposalsByProject(
            @PathVariable Long projectId) {
        return proposalRepository.findByProjectId(projectId);
    }

    // Accept a proposal
    @PutMapping("/{id}/accept")
    public Proposal acceptProposal(@PathVariable Long id) {

        Proposal proposal = proposalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proposal not found"));

        proposal.setStatus("ACCEPTED");

        return proposalRepository.save(proposal);
    }

    // Reject a proposal
    @PutMapping("/{id}/reject")
    public Proposal rejectProposal(@PathVariable Long id) {

        Proposal proposal = proposalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proposal not found"));

        proposal.setStatus("REJECTED");

        return proposalRepository.save(proposal);
    }
}