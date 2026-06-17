package com.hiretrack.job;

import java.time.LocalDate;

public class JobApplicationResponse {

    private Long id;
    private String company;
    private String role;
    private ApplicationStatus status;
    private LocalDate appliedDate;
    private String userEmail;

    public JobApplicationResponse(JobApplication application) {
        this.id = application.getId();
        this.company = application.getCompany();
        this.role = application.getRole();
        this.status = application.getStatus();
        this.appliedDate = application.getAppliedDate();
        this.userEmail = application.getUser().getEmail();
    }

    public Long getId() { return id; }
    public String getCompany() { return company; }
    public String getRole() { return role; }
    public ApplicationStatus getStatus() { return status; }
    public LocalDate getAppliedDate() { return appliedDate; }
    public String getUserEmail() { return userEmail; }
}