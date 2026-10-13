package com.example.NotesApi.service;

import com.example.NotesApi.dto.UserRequest;
import com.example.NotesApi.dto.UserResponse;
import com.example.NotesApi.entity.User;
import com.example.NotesApi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.example.NotesApi.exception.UserAlreadyExistsException;
import com.example.NotesApi.exception.UserNotFoundException;
import java.util.List;
import java.util.stream.Collectors;
import com.example.NotesApi.dto.AuthRequest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    // Получить всех пользователей как DTO
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }


    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        return toResponse(user);
    }

    // Создать: Request → Entity → save → Response
    public UserResponse createUser(UserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("User with email " + request.getEmail() + " already exists");
        }
        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(request.getPassword())  // TODO: захешировать позже
                .fullName(request.getFullName())
                .build();
        User saved = userRepository.save(user);
        return toResponse(saved);
    }

    // Обновить: Request → найти → обновить → save → Response
    public UserResponse updateUser(Long id, UserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        user.setEmail(request.getEmail());
        user.setPasswordHash(request.getPassword());  // TODO: захешировать позже
        user.setFullName(request.getFullName());
        User updated = userRepository.save(user);
        return toResponse(updated);
    }

    // Удалить
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
    public User createUserEntity(AuthRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException(
                    "User with email " + request.getEmail() + " already exists"
            );
        }
        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .build();
        return userRepository.save(user);
    }

    // Загрузить UserDetails по email (для генерации токена)
    public UserDetails loadUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
    }
    // Преобразование Entity → Response DTO
    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .createdAt(user.getCreatedAt())
                .build();
    }
}