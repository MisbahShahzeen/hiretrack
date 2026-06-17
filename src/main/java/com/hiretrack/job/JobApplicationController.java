package com.hiretrack.job;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/jobs")
public class JobApplicationController {

    private final JobApplicationService jobApplicationService;

    public JobApplicationController(JobApplicationService jobApplicationService) {
        this.jobApplicationService = jobApplicationService;
    }

    @PostMapping
    public ResponseEntity<JobApplicationResponse> create(
            @RequestBody JobApplicationRequest request,
            Principal principal) {

        JobApplication created = jobApplicationService.create(
                principal.getName(),
                request.getCompany(),
                request.getRole(),
                request.getStatus());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new JobApplicationResponse(created));
    }

    @GetMapping
    public ResponseEntity<List<JobApplicationResponse>> list(
            @RequestParam(required = false) ApplicationStatus status,
            @RequestParam(required = false) String sortBy,
            Principal principal) {

        List<JobApplicationResponse> applications =
                jobApplicationService.listForUser(principal.getName(), status, sortBy)
                        .stream()
                        .map(JobApplicationResponse::new)
                        .toList();
        return ResponseEntity.ok(applications);
    }

    @PutMapping("/{id}")
    public ResponseEntity<JobApplicationResponse> update(
            @PathVariable Long id,
            @RequestBody JobApplicationRequest request,
            Principal principal) {

        JobApplication updated = jobApplicationService.update(
                principal.getName(),
                id,
                request.getCompany(),
                request.getRole(),
                request.getStatus());

        return ResponseEntity.ok(new JobApplicationResponse(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Principal principal) {
        jobApplicationService.delete(principal.getName(), id);
        return ResponseEntity.noContent().build();
    }
}