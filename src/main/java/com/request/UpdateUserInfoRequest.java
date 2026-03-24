package com.request;

import lombok.Data;

import java.time.LocalDate;


@Data
public class UpdateUserInfoRequest {
    private String fullName;
    private LocalDate dob;
}

