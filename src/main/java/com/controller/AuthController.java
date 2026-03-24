package com.controller;

import com.exception.UserAccountException;
import com.request.ChangePasswordRequest;
import com.request.LoginRequest;
import com.request.UserRegisterRequest;
import com.response.ApiResponse;
import com.response.AuthRespone;
import com.service.UserAccountService;
import jakarta.mail.MessagingException;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;


@RestController
@RequestMapping("/auth")

public class AuthController {
    private UserAccountService userService;

    public AuthController(UserAccountService userAccountService){
        this.userService=userAccountService;
    }

    @Value("${domain}")
    private String domain;

    @PostMapping("/register")
    public ResponseEntity<?> createUserHandler(@RequestBody UserRegisterRequest userRegister) throws UserAccountException, MessagingException {

        userService.register(userRegister);

        ApiResponse apiResponse=new ApiResponse("Đăng kí thành công, vui lòng kiểm tra email để kích hoạt tài khoản!",true);
        return new ResponseEntity<ApiResponse>(apiResponse, HttpStatus.OK);
    }


    @GetMapping("/auth-account")
    public ResponseEntity<Void> authAccountHandler(
            @RequestParam("key") String key,
            @RequestParam("email") String email){

        AuthRespone res=userService.authAccount(email,key);
        String frontendUrl = domain+"/login";
        if (res!=null) {
            URI uri = URI.create(frontendUrl + "?verified=true");
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(uri)
                    .build();
        } else {
            URI uri = URI.create(frontendUrl + "?verified=false");
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(uri)
                    .build();
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthRespone> loginHandler(@RequestBody LoginRequest loginRequest) {

        AuthRespone res=userService.login(loginRequest);
        return new ResponseEntity<AuthRespone>(res, HttpStatus.OK);

    }

    @PostMapping("/changePassword")
    public ResponseEntity<ApiResponse> changePasswordHandler(@RequestBody ChangePasswordRequest changePasswordRequest) {

        boolean success=userService.changePass(changePasswordRequest);
        ApiResponse response=new ApiResponse("Fail",false);;
        if(success){
            response=new ApiResponse("Success",true);
        }
        return new ResponseEntity<ApiResponse>(response, HttpStatus.OK);
    }

}
