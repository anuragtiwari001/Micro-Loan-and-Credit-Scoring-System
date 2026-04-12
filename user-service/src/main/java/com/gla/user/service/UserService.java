package com.gla.user.service;
import com.gla.common_service.enums.Role;
import com.gla.common_service.security.JwtUtil;
import com.gla.user.dto.LoginRequest;
import com.gla.user.entity.User;
import com.gla.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
@Service
public class UserService {
    @Autowired
    private BCryptPasswordEncoder encoder;
    @Autowired
    private UserRepository userRepository;
    public String register(User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("User already exists");
        }
        if (user.getRole() == null) {
            user.setRole(Role.USER);  // ✅ ENUM
        }
        user.setPassword(encoder.encode(user.getPassword()));
        userRepository.save(user);
        return "User registered successfully";
    }
    public String login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!encoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }
        Role role = user.getRole();  // ✅ DIRECT
        return JwtUtil.generateToken(user.getEmail(), role);
    }

    public String getEmailById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getEmail();
    }
}