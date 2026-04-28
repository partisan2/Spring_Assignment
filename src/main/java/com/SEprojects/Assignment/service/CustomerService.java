package com.SEprojects.Assignment.service;

import com.SEprojects.Assignment.dto.CustomerDTO;
import com.SEprojects.Assignment.dto.CustomerResponseDTO;

import java.util.List;

public interface CustomerService {
    CustomerResponseDTO getCustomerById(long id);
    Long createCustomer(CustomerDTO customerDTO);
    void updateCustomer(Long id, CustomerDTO customerDTO);
    void addFamilyMembers(Long customerId, List<Long> familyMemberIds);
    void addFamilyMembersByNic(Long customerId, List<String> nics);
    List<CustomerResponseDTO> getAllCustomers();
}
