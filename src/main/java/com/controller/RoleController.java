package com.controller;//package com.controller.auth;

import com.entity.Role;
import com.request.CreateRoleRequest;
import com.response.ApiResponse;
import com.service.RoleService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
@RequestMapping("/role")
public class RoleController {
    private RoleService roleService;


    @PostMapping("/create")
    public ResponseEntity<ApiResponse> createAmenity(@RequestBody CreateRoleRequest createRoleRequest) {
        Role role=Role.builder().name(createRoleRequest.getName())
                .description(createRoleRequest.getDescription())
                .build();
        roleService.saveRole(role);


        return new ResponseEntity<ApiResponse>(new ApiResponse("Success", true), HttpStatus.CREATED);
    }



}
