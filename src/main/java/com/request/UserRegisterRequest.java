package com.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Date;

@AllArgsConstructor
@Setter
@Getter
public class UserRegisterRequest {
    private String username;
    private String password;
    private String fullName;
    private LocalDate dob;
    private String phoneNumber;
    private String email;
    private String avt;
}
