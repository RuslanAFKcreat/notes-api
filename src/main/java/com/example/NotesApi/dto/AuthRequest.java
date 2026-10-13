package com.example.NotesApi.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthRequest {

    private String email;
    private String password;
    private String fullName;  // нужен только при регистрации
}