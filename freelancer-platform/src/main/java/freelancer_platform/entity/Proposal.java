package freelancer_platform.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Proposal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long projectId;
    private String freelancerEmail;
    private String coverLetter;
    private double proposedAmount;
    private String status = "PENDING";

    // Default constructor
    public Proposal() {
    }

    // Constructor
    public Proposal(Long projectId, String freelancerEmail,
                    String coverLetter, double proposedAmount) {
        this.projectId = projectId;
        this.freelancerEmail = freelancerEmail;
        this.coverLetter = coverLetter;
        this.proposedAmount = proposedAmount;
        this.status = "PENDING";
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
}