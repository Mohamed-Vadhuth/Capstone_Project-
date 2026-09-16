package freelancer_platform.dto;

import java.util.List;

public class ClientDashboardDto {

    private UserProfileDto clientProfile;
    private int totalProjects;
    private int openProjects;
    private int inProgressProjects;
    private int completedProjects;
    private int totalProposalsReceived;
    private List<ProjectResponseDto> projects;

    public ClientDashboardDto() {
    }

    public ClientDashboardDto(UserProfileDto clientProfile, int totalProjects, int openProjects,
                              int inProgressProjects, int completedProjects,
                              int totalProposalsReceived, List<ProjectResponseDto> projects) {
        this.clientProfile = clientProfile;
        this.totalProjects = totalProjects;
        this.openProjects = openProjects;
        this.inProgressProjects = inProgressProjects;
        this.completedProjects = completedProjects;
        this.totalProposalsReceived = totalProposalsReceived;
        this.projects = projects;
    }

    public UserProfileDto getClientProfile() {
        return clientProfile;
    }

    public void setClientProfile(UserProfileDto clientProfile) {
        this.clientProfile = clientProfile;
    }

    public int getTotalProjects() {
        return totalProjects;
    }

    public void setTotalProjects(int totalProjects) {
        this.totalProjects = totalProjects;
    }

    public int getOpenProjects() {
        return openProjects;
    }

    public void setOpenProjects(int openProjects) {
        this.openProjects = openProjects;
    }

    public int getInProgressProjects() {
        return inProgressProjects;
    }

    public void setInProgressProjects(int inProgressProjects) {
        this.inProgressProjects = inProgressProjects;
    }

    public int getCompletedProjects() {
        return completedProjects;
    }

    public void setCompletedProjects(int completedProjects) {
        this.completedProjects = completedProjects;
    }

    public int getTotalProposalsReceived() {
        return totalProposalsReceived;
    }

    public void setTotalProposalsReceived(int totalProposalsReceived) {
        this.totalProposalsReceived = totalProposalsReceived;
    }

    public List<ProjectResponseDto> getProjects() {
        return projects;
    }

    public void setProjects(List<ProjectResponseDto> projects) {
        this.projects = projects;
    }
}
