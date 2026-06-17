package com.hiretrack.job;

import com.hiretrack.user.User;
import com.hiretrack.user.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

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
}