package com.SEprojects.Assignment.controller;


import com.SEprojects.Assignment.dto.CustomerDTO;
import com.SEprojects.Assignment.dto.CustomerFamilyMemberDTO;
import com.SEprojects.Assignment.dto.CustomerResponseDTO;
import com.SEprojects.Assignment.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    public ResponseEntity<?> addFamilyMembers(@PathVariable Long id, @RequestBody List<CustomerFamilyMemberDTO> familyMembers) {
        try {
            customerService.addFamilyMembers(id, familyMembers);
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

    @PostMapping(value = "/bulk", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    @io.swagger.v3.oas.annotations.Operation(summary = "Bulk upload customers from Excel file")
    public ResponseEntity<String> bulkUpload(
            @io.swagger.v3.oas.annotations.Parameter(description = "Excel file with columns: Name, DOB, NIC, Mobile, AddressLine1, AddressLine2, City, Country")
            @RequestPart("file") MultipartFile file) {
        try {
            // Copy file to temp location in the request thread to ensure it exists for the async task
            java.nio.file.Path tempFile = java.nio.file.Files.createTempFile("bulk-upload-", ".xlsx");
            try (java.io.InputStream is = file.getInputStream()) {
                java.nio.file.Files.copy(is, tempFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
            
            customerService.processBulkFileAsync(tempFile.toFile());
            return ResponseEntity.accepted().body("Bulk upload started. Processing in background.");
        } catch (java.io.IOException e) {
            return ResponseEntity.internalServerError().body("Failed to prepare file: " + e.getMessage());
        }
    }
}
