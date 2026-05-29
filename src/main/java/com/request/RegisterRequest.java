package com.request;

import lombok.*;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class RegisterRequest {
    private String username;

    private String password;

    private String fullName;

    private LocalDate dob;

    private String phoneNumber;

    private String email;

    private String avt;

    private String address;

    private String city;

    private String district;

    private String commune;

}
