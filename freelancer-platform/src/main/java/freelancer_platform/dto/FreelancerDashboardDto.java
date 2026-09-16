package freelancer_platform.dto;

import java.util.List;

public class FreelancerDashboardDto {

    private UserProfileDto freelancerProfile;
    private int totalProposals;
    private int pendingProposals;
    private int acceptedProposals;
    private int rejectedProposals;
    private int completedProjects;
    private List<ProposalResponseDto> proposals;

    public FreelancerDashboardDto() {
    }

    public FreelancerDashboardDto(UserProfileDto freelancerProfile, int totalProposals,
                                  int pendingProposals, int acceptedProposals, int rejectedProposals,
                                  int completedProjects, List<ProposalResponseDto> proposals) {
        this.freelancerProfile = freelancerProfile;
        this.totalProposals = totalProposals;
        this.pendingProposals = pendingProposals;
        this.acceptedProposals = acceptedProposals;
        this.rejectedProposals = rejectedProposals;
        this.completedProjects = completedProjects;
        this.proposals = proposals;
    }

    public UserProfileDto getFreelancerProfile() {
        return freelancerProfile;
    }

    public void setFreelancerProfile(UserProfileDto freelancerProfile) {
        this.freelancerProfile = freelancerProfile;
    }

    public int getTotalProposals() {
        return totalProposals;
    }

    public void setTotalProposals(int totalProposals) {
        this.totalProposals = totalProposals;
    }

    public int getPendingProposals() {
        return pendingProposals;
    }

    public void setPendingProposals(int pendingProposals) {
        this.pendingProposals = pendingProposals;
    }

    public int getAcceptedProposals() {
        return acceptedProposals;
    }

    public void setAcceptedProposals(int acceptedProposals) {
        this.acceptedProposals = acceptedProposals;
    }

    public int getRejectedProposals() {
        return rejectedProposals;
    }

    public void setRejectedProposals(int rejectedProposals) {
        this.rejectedProposals = rejectedProposals;
    }

    public int getCompletedProjects() {
        return completedProjects;
    }

    public void setCompletedProjects(int completedProjects) {
        this.completedProjects = completedProjects;
    }

    public List<ProposalResponseDto> getProposals() {
        return proposals;
    }

    public void setProposals(List<ProposalResponseDto> proposals) {
        this.proposals = proposals;
    }
}
