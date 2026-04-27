package com.SEprojects.Assignment.service;

import com.SEprojects.Assignment.dto.CustomerDTO;

public interface CustomerService {
    CustomerDTO getCustomerById(long id);
    Long createCustomer(CustomerDTO customerDTO);
}
