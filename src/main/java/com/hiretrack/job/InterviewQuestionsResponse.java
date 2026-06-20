package com.hiretrack.job;

public class InterviewQuestionsResponse {

    private Long jobId;
    private String company;
    private String role;
    private String questions;

    public InterviewQuestionsResponse(Long jobId, String company, String role, String questions) {
        this.jobId = jobId;
        this.company = company;
        this.role = role;
        this.questions = questions;
    }

    public Long getJobId() { return jobId; }
    public String getCompany() { return company; }
    public String getRole() { return role; }
    public String getQuestions() { return questions; }
}