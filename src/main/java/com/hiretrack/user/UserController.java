package com.hiretrack.user;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        User created = userService.register(
                request.getEmail(),
                request.getPassword(),
                request.getFullName()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("User registered with id: " + created.getId());
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest request) {
        String token = userService.login(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(token);
    }
    @GetMapping("/me")
    public ResponseEntity<String> me(java.security.Principal principal) {
        return ResponseEntity.ok("You are authenticated as: " + principal.getName());
    }
}