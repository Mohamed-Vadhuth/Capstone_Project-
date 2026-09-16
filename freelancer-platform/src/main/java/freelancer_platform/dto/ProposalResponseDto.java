package freelancer_platform.dto;

import java.time.LocalDateTime;

public class ProposalResponseDto {

    private Long id;
    private Long projectId;
    private String projectTitle;
    private String projectStatus;
    private String freelancerEmail;
    private String freelancerName;
    private String coverLetter;
    private double proposedAmount;
    private String status;
    private String estimatedDelivery;
    private LocalDateTime createdAt;

    public ProposalResponseDto() {
    }

    public ProposalResponseDto(Long id, Long projectId, String projectTitle, String projectStatus,
                               String freelancerEmail, String freelancerName, String coverLetter,
                               double proposedAmount, String status, String estimatedDelivery,
                               LocalDateTime createdAt) {
        this.id = id;
        this.projectId = projectId;
        this.projectTitle = projectTitle;
        this.projectStatus = projectStatus;
        this.freelancerEmail = freelancerEmail;
        this.freelancerName = freelancerName;
        this.coverLetter = coverLetter;
        this.proposedAmount = proposedAmount;
        this.status = status;
        this.estimatedDelivery = estimatedDelivery;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getProjectTitle() {
        return projectTitle;
    }

    public void setProjectTitle(String projectTitle) {
        this.projectTitle = projectTitle;
    }

    public String getProjectStatus() {
        return projectStatus;
    }

    public void setProjectStatus(String projectStatus) {
        this.projectStatus = projectStatus;
    }

    public String getFreelancerEmail() {
        return freelancerEmail;
    }

    public void setFreelancerEmail(String freelancerEmail) {
        this.freelancerEmail = freelancerEmail;
    }

    public String getFreelancerName() {
        return freelancerName;
    }

    public void setFreelancerName(String freelancerName) {
        this.freelancerName = freelancerName;
    }

    public String getCoverLetter() {
        return coverLetter;
    }

    public void setCoverLetter(String coverLetter) {
        this.coverLetter = coverLetter;
    }

    public double getProposedAmount() {
        return proposedAmount;
    }

    public void setProposedAmount(double proposedAmount) {
        this.proposedAmount = proposedAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getEstimatedDelivery() {
        return estimatedDelivery;
    }

    public void setEstimatedDelivery(String estimatedDelivery) {
        this.estimatedDelivery = estimatedDelivery;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
