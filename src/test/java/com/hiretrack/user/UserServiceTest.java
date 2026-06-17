package com.hiretrack.user;

import com.hiretrack.config.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserService userService;

    @Test
    void register_shouldThrow_whenEmailAlreadyExists() {
        // Arrange: program the mock — pretend this email is already taken
        when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

        // Act + Assert: registering with that email should throw
        assertThatThrownBy(() ->
                userService.register("taken@example.com", "password123", "Some Name"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email already registered");
    }

    @Test
    void register_shouldSucceed_whenEmailIsNew() {
        // Arrange: email is free, and saving returns the user back
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(userRepository.save(org.mockito.ArgumentMatchers.any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User result = userService.register("new@example.com", "password123", "New User");

        // Assert: the returned user has the right fields, and the password was hashed (not raw)
        org.assertj.core.api.Assertions.assertThat(result.getEmail()).isEqualTo("new@example.com");
        org.assertj.core.api.Assertions.assertThat(result.getFullName()).isEqualTo("New User");
        org.assertj.core.api.Assertions.assertThat(result.getPassword()).isNotEqualTo("password123");
    }

    @Test
    void login_shouldThrow_whenUserNotFound() {
        // Arrange: no user with this email
        when(userRepository.findByEmail("ghost@example.com"))
                .thenReturn(java.util.Optional.empty());

        // Act + Assert
        assertThatThrownBy(() ->
                userService.login("ghost@example.com", "whatever"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid email or password");
    }
}