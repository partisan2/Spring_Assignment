package com.SEprojects.Assignment.service;

import com.SEprojects.Assignment.dto.CustomerDTO;
import com.SEprojects.Assignment.dto.CustomerFamilyMemberDTO;
import com.SEprojects.Assignment.dto.CustomerResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CustomerService {
    CustomerResponseDTO getCustomerById(long id);
    Long createCustomer(CustomerDTO customerDTO);
    void updateCustomer(Long id, CustomerDTO customerDTO);
    void addFamilyMembers(Long customerId, List<CustomerFamilyMemberDTO> familyMembers);
    void addFamilyMembersByNic(Long customerId, List<String> nics);
    List<CustomerResponseDTO> getAllCustomers();
    void processBulkFileAsync(java.io.File file);
}
