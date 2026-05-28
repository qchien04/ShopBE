package com.controller;


import com.DTO.UserAccountDTO;
import com.request.ImageUrlUpdateRequest;
import com.request.UpdateUserInfoRequest;
import com.service.UserAccountService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/users")
@AllArgsConstructor
public class UserController {
    private UserAccountService userService;

    @GetMapping("/")
    public ResponseEntity<String> getUserTokenHandler(@RequestHeader("Authorization") String token){
        return new ResponseEntity<String>(token, HttpStatus.OK);
    }

    @GetMapping("/me")
    public ResponseEntity<UserAccountDTO> getMyProfileHandler() {
        Long myId = ((Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal());

        UserAccountDTO user=userService.getProfile(myId);
        return new ResponseEntity<UserAccountDTO>(user, HttpStatus.OK);
    }


    @GetMapping("/profile/{userId}")
    public ResponseEntity<UserAccountDTO> getUserProfileHandler(@PathVariable("userId") Long userId) {
        UserAccountDTO user=userService.getProfile(userId);
        return new ResponseEntity<UserAccountDTO>(user, HttpStatus.OK);
    }

    @PostMapping("/update-info")
    public ResponseEntity<UserAccountDTO> changeInfoHandle(@RequestBody UpdateUserInfoRequest request) {
        UserAccountDTO userProfile=userService.updateInfo(request);

        return new ResponseEntity<>(userProfile, HttpStatus.OK);
    }

    @PostMapping("/update-avt")
    public ResponseEntity<UserAccountDTO> changeAvtHandle(@RequestBody ImageUrlUpdateRequest request) {
        UserAccountDTO userProfile=userService.updateAvt(request.getImageUrl());
        return new ResponseEntity<>(userProfile, HttpStatus.OK);
    }
}
