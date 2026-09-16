package freelancer_platform.dto;

import java.time.LocalDateTime;

public class ProjectResponseDto {

    private Long id;
    private String title;
    private String description;
    private double budget;
    private String category;
    private String status;
    private String clientEmail;
    private LocalDateTime createdAt;
    private int proposalCount;

    public ProjectResponseDto() {
    }

    public ProjectResponseDto(Long id, String title, String description, double budget,
                              String category, String status, String clientEmail,
                              LocalDateTime createdAt, int proposalCount) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.budget = budget;
        this.category = category;
        this.status = status;
        this.clientEmail = clientEmail;
        this.createdAt = createdAt;
        this.proposalCount = proposalCount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getBudget() {
        return budget;
    }

    public void setBudget(double budget) {
        this.budget = budget;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getClientEmail() {
        return clientEmail;
    }

    public void setClientEmail(String clientEmail) {
        this.clientEmail = clientEmail;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public int getProposalCount() {
        return proposalCount;
    }

    public void setProposalCount(int proposalCount) {
        this.proposalCount = proposalCount;
    }
}
