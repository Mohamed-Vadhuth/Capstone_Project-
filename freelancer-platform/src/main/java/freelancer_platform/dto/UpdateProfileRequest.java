package freelancer_platform.dto;

import jakarta.validation.constraints.NotBlank;

public class UpdateProfileRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private String bio;

    private String skills;

    private String experience;

    private String portfolioUrl;

    public UpdateProfileRequest() {
    }

    public UpdateProfileRequest(String name, String bio, String skills, String experience, String portfolioUrl) {
        this.name = name;
        this.bio = bio;
        this.skills = skills;
        this.experience = experience;
        this.portfolioUrl = portfolioUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }

    public String getExperience() {
        return experience;
    }

    public void setExperience(String experience) {
        this.experience = experience;
    }

    public String getPortfolioUrl() {
        return portfolioUrl;
    }

    public void setPortfolioUrl(String portfolioUrl) {
        this.portfolioUrl = portfolioUrl;
    }
}
