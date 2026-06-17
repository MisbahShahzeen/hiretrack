package com.hiretrack.job;

import com.hiretrack.user.User;
import com.hiretrack.user.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

import com.hiretrack.common.ResourceNotFoundException;

@Service
public class JobApplicationService {

    private final JobApplicationRepository jobApplicationRepository;
    private final UserRepository userRepository;

    public JobApplicationService(JobApplicationRepository jobApplicationRepository,
                                 UserRepository userRepository) {
        this.jobApplicationRepository = jobApplicationRepository;
        this.userRepository = userRepository;
    }

    public JobApplication create(String email, String company, String role, ApplicationStatus status) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        JobApplication application = new JobApplication(
                company, role, status, LocalDate.now(), user);
        return jobApplicationRepository.save(application);
    }

    public List<JobApplication> listForUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return jobApplicationRepository.findByUser(user);
    }

    public JobApplication update(String email, Long id, String company, String role, ApplicationStatus status) {
        JobApplication application = getOwnedApplication(email, id);

        application.setCompany(company);
        application.setRole(role);
        application.setStatus(status);

        return jobApplicationRepository.save(application);
    }

    public void delete(String email, Long id) {
        JobApplication application = getOwnedApplication(email, id);
        jobApplicationRepository.delete(application);
    }

    // Fetches an application only if it belongs to the given user.
    // Treats "not yours" the same as "doesn't exist" (no information leak).
    private JobApplication getOwnedApplication(String email, Long id) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        JobApplication application = jobApplicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job application not found"));

        if (!application.getUser().getId().equals(user.getId())) {
            // Application exists but belongs to someone else — pretend it doesn't exist
            throw new ResourceNotFoundException("Job application not found");
        }

        return application;
    }
}