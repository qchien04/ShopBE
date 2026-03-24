package com.service;


import com.entity.OTPCode;

public interface OTPCodeService {
    OTPCode findOTPCode(String mail, String data);
    void saveOTPCode(OTPCode otpCode);
}
