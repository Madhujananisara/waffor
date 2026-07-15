package com.waffor.orderservice.controller;

import com.waffor.orderservice.entity.User;
import com.waffor.orderservice.repository.UserRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        log.info("Received register request for email: {}", request.getEmail());
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Email is already registered!");
            return ResponseEntity.badRequest().body(error);
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(request.getPassword()) // Plain text for simplicity/demo
                .name(request.getName())
                .mobile(request.getMobile())
                .address(request.getAddress())
                .role(request.getRole() != null ? request.getRole() : "CUSTOMER")
                .build();

        User savedUser = userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        log.info("Received login request for email: {}", request.getEmail());
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
        
        if (userOpt.isPresent() && userOpt.get().getPassword().equals(request.getPassword())) {
            return ResponseEntity.ok(userOpt.get());
        }

        Map<String, String> error = new HashMap<>();
        error.put("message", "Invalid email or password!");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        log.info("Received forgot-password request for email: {}", request.getEmail());
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setPassword(request.getNewPassword());
            userRepository.save(user);
            
            Map<String, String> success = new HashMap<>();
            success.put("message", "Password reset successfully!");
            return ResponseEntity.ok(success);
        }

        Map<String, String> error = new HashMap<>();
        error.put("message", "Email address not found!");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @PutMapping("/profile/{id}")
    public ResponseEntity<?> updateProfile(@PathVariable("id") Long id, @RequestBody RegisterRequest request) {
        log.info("Received profile update request for user ID: {}", id);
        return userRepository.findById(id)
                .map(user -> {
                    user.setName(request.getName());
                    user.setMobile(request.getMobile());
                    user.setAddress(request.getAddress());
                    User saved = userRepository.save(user);
                    return ResponseEntity.ok(saved);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @Data
    public static class RegisterRequest {
        private String email;
        private String password;
        private String name;
        private String mobile;
        private String address;
        private String role;
    }

    @Data
    public static class LoginRequest {
        private String email;
        private String password;
    }

    @Data
    public static class ForgotPasswordRequest {
        private String email;
        private String newPassword;
    }
}
