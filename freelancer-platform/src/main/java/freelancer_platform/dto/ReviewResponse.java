package freelancer_platform.dto;

import java.time.LocalDateTime;

public class ReviewResponse {

    private Long id;
    private Long projectId;
    private String reviewerEmail;
    private String reviewerName;
    private String reviewedUserEmail;
    private int rating;
    private String comment;
    private LocalDateTime createdAt;

    public ReviewResponse() {
    }

    public ReviewResponse(Long id, Long projectId, String reviewerEmail, String reviewerName,
                          String reviewedUserEmail, int rating, String comment, LocalDateTime createdAt) {
        this.id = id;
        this.projectId = projectId;
        this.reviewerEmail = reviewerEmail;
        this.reviewerName = reviewerName;
        this.reviewedUserEmail = reviewedUserEmail;
        this.rating = rating;
        this.comment = comment;
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

    public String getReviewerEmail() {
        return reviewerEmail;
    }

    public void setReviewerEmail(String reviewerEmail) {
        this.reviewerEmail = reviewerEmail;
    }

    public String getReviewerName() {
        return reviewerName;
    }

    public void setReviewerName(String reviewerName) {
        this.reviewerName = reviewerName;
    }

    public String getReviewedUserEmail() {
        return reviewedUserEmail;
    }

    public void setReviewedUserEmail(String reviewedUserEmail) {
        this.reviewedUserEmail = reviewedUserEmail;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
