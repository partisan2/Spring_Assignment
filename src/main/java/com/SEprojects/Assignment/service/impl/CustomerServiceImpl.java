package com.SEprojects.Assignment.service.impl;

import com.SEprojects.Assignment.dto.CustomerDTO;
import com.SEprojects.Assignment.model.Customer;
import com.SEprojects.Assignment.repo.CustomerRepo;
import com.SEprojects.Assignment.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CustomerServiceImpl implements CustomerService {

    @Autowired
    private CustomerRepo customerRepo;

    @Override
    public CustomerDTO getCustomerById(long id) {
        Customer customer = customerRepo.findCustomerById(id);
        return null;
    }

    @Override
    public Long createCustomer(CustomerDTO customerDTO) {
        return 0L;
    }
}
