package com.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserAccountDTO {
    private Long id;

    private String username;

    private String password;

    private String fullName;

    private LocalDate dob;

    private String email;

    private String avt;

    private String phoneNumber;

    private List<String> roles;
}
