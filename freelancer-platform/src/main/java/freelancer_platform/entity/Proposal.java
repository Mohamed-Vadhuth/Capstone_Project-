package freelancer_platform.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

@Entity
public class Proposal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Project ID is required")
    @Positive(message = "Project ID must be a positive number")
    private Long projectId;

    @NotBlank(message = "Freelancer email is required")
    @Email(message = "Freelancer email must be valid")
    private String freelancerEmail;

    @NotBlank(message = "Cover letter is required")
    private String coverLetter;

    @Positive(message = "Proposed amount must be greater than zero")
    private double proposedAmount;

    private String status = "PENDING";

    private String estimatedDelivery;

    private LocalDateTime createdAt;

    // Default constructor
    public Proposal() {
    }

    // Constructor preserving original signature
    public Proposal(Long projectId, String freelancerEmail,
                    String coverLetter, double proposedAmount) {
        this.projectId = projectId;
        this.freelancerEmail = freelancerEmail;
        this.coverLetter = coverLetter;
        this.proposedAmount = proposedAmount;
        this.status = "PENDING";
    }

    public Proposal(Long projectId, String freelancerEmail,
                    String coverLetter, double proposedAmount, String estimatedDelivery) {
        this.projectId = projectId;
        this.freelancerEmail = freelancerEmail;
        this.coverLetter = coverLetter;
        this.proposedAmount = proposedAmount;
        this.estimatedDelivery = estimatedDelivery;
        this.status = "PENDING";
    }

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.status == null || this.status.isBlank()) {
            this.status = "PENDING";
        }
    }

    // Get ID
    public Long getId() {
        return id;
    }

    // Get Project ID
    public Long getProjectId() {
        return projectId;
    }

    // Set Project ID
    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    // Get Freelancer Email
    public String getFreelancerEmail() {
        return freelancerEmail;
    }

    // Set Freelancer Email
    public void setFreelancerEmail(String freelancerEmail) {
        this.freelancerEmail = freelancerEmail;
    }

    // Get Cover Letter
    public String getCoverLetter() {
        return coverLetter;
    }

    // Set Cover Letter
    public void setCoverLetter(String coverLetter) {
        this.coverLetter = coverLetter;
    }

    // Get Proposed Amount
    public double getProposedAmount() {
        return proposedAmount;
    }

    // Set Proposed Amount
    public void setProposedAmount(double proposedAmount) {
        this.proposedAmount = proposedAmount;
    }

    // Get Status
    public String getStatus() {
        return status;
    }

    // Set Status
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