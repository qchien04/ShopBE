package com.controller;

import com.DTO.CustomerAddressDTO;
import com.request.CreateCustomerAddressRequest;
import com.request.UpdateCustomerAddressRequest;
import com.response.ApiResponse;
import com.service.implement.CustomerAddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/address")
@RequiredArgsConstructor
public class CustomerAddressController {
    private final CustomerAddressService customerAddressService;

    @GetMapping
    public ResponseEntity<List<CustomerAddressDTO>> getAllOrders() {
        return ResponseEntity.ok(customerAddressService.getUserAddressDTO());
    }

    @PostMapping
    public ResponseEntity<CustomerAddressDTO> createAddress(@RequestBody CreateCustomerAddressRequest request) {
        CustomerAddressDTO customerAddress = customerAddressService.createCustomerAddress(request);
        return ResponseEntity.ok(customerAddress);
    }

    @PutMapping
    public ResponseEntity<CustomerAddressDTO> updateAddress(@RequestBody UpdateCustomerAddressRequest request) {
        CustomerAddressDTO customerAddress = customerAddressService.updateCustomerAddress(request);
        return ResponseEntity.ok(customerAddress);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> cancelOrder(@PathVariable Long id) {
        customerAddressService.deleteAddress(id);
        ApiResponse apiResponse=new ApiResponse("Successfully!",true);
        return ResponseEntity.ok(apiResponse);
    }
}
