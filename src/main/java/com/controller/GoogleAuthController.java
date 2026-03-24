package com.controller;

import com.response.AuthRespone;
import com.service.UserAccountService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping("/google/login")
@AllArgsConstructor
public class GoogleAuthController{

    private UserAccountService userService;

    @PostMapping("/user")
    public ResponseEntity<?> loginWithGoogle(@RequestBody Map<String, String> request) {

        AuthRespone apiResponse=userService.loginWithGoogle(request);

        return new ResponseEntity<AuthRespone>(apiResponse, HttpStatus.OK);
    }

}
