package com.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class UserAccountDTO {
    private Long id;

    private String username;

    private String password;

    private String fullName;

    private LocalDate dob;

    private String email;

    private List<String> roles;
}
