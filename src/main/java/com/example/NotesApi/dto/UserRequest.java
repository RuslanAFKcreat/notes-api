package com.example.NotesApi.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRequest {

    private String email;
    private String password;
    private String fullName;
}