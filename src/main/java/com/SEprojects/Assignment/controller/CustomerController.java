package com.SEprojects.Assignment.controller;


import com.SEprojects.Assignment.dto.CustomerDTO;
import com.SEprojects.Assignment.dto.CustomerResponseDTO;
import com.SEprojects.Assignment.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@CrossOrigin(origins = "*")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @GetMapping
    public List<CustomerResponseDTO> getAllCustomers() {
        return customerService.getAllCustomers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponseDTO> getCustomerById(@PathVariable Long id) {
        CustomerResponseDTO customerDTO = customerService.getCustomerById(id);

        if(customerDTO != null) {
            return ResponseEntity.ok(customerDTO);
        }

        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<?> createCustomer(@RequestBody CustomerDTO customerDTO) {
        try {
            Long id = customerService.createCustomer(customerDTO);
            return ResponseEntity.ok(id);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCustomer(@PathVariable Long id, @RequestBody CustomerDTO customerDTO) {
        try {
            customerService.updateCustomer(id, customerDTO);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/family")
    public ResponseEntity<?> addFamilyMembers(@PathVariable Long id, @RequestBody List<Long> familyMemberIds) {
        try {
            customerService.addFamilyMembers(id, familyMemberIds);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/family-by-nic")
    public ResponseEntity<?> addFamilyMembersByNic(@PathVariable Long id, @RequestBody List<String> nics) {
        try {
            customerService.addFamilyMembersByNic(id, nics);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
