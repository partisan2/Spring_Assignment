package com.SEprojects.Assignment.service.impl;

import com.SEprojects.Assignment.dto.AddressDTO;
import com.SEprojects.Assignment.dto.AddressResponseDTO;
import com.SEprojects.Assignment.dto.CustomerDTO;
import com.SEprojects.Assignment.dto.CustomerMobileDTO;
import com.SEprojects.Assignment.dto.CustomerMobileResponseDTO;
import com.SEprojects.Assignment.dto.CustomerResponseDTO;
import com.SEprojects.Assignment.model.Address;
import com.SEprojects.Assignment.model.Customer;
import com.SEprojects.Assignment.model.CustomerMobile;
import com.SEprojects.Assignment.repo.CustomerRepo;
import com.SEprojects.Assignment.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomerServiceImpl implements CustomerService {

    @Autowired
    private CustomerRepo customerRepo;

    @Override
    public CustomerResponseDTO getCustomerById(long id) {
        Customer customer = customerRepo.findCustomerById(id);
        return convertToResponseDTO(customer);
    }

    @Override
    public Long createCustomer(CustomerDTO customerDTO) {
        validateCustomer(customerDTO, null, false);
        Customer customer = convertToEntity(customerDTO);
        return customerRepo.saveCustomer(customer);
    }

    @Override
    public void updateCustomer(Long id, CustomerDTO customerDTO) {
        if (id == null) {
            throw new RuntimeException("Customer ID is mandatory for update.");
        }
        validateCustomer(customerDTO, id, true);
        Customer customer = convertToEntity(customerDTO);
        customer.setId(id);
        customerRepo.updateCustomer(customer);
    }

    @Override
    public void addFamilyMembers(Long customerId, List<Long> familyMemberIds) {
        if (customerRepo.findCustomerById(customerId) == null) {
            throw new RuntimeException("Customer not found.");
        }
        List<Long> validIds = familyMemberIds.stream()
                .filter(id -> id != null && id > 0 && !id.equals(customerId))
                .collect(Collectors.toList());
        customerRepo.addFamilyMembers(customerId, validIds);
    }

    @Override
    public void addFamilyMembersByNic(Long customerId, List<String> nics) {
        if (customerRepo.findCustomerById(customerId) == null) {
            throw new RuntimeException("Customer not found.");
        }
        
        List<Long> familyMemberIds = nics.stream()
                .filter(nic -> nic != null && !nic.isEmpty())
                .map(nic -> customerRepo.findIdByNic(nic))
                .filter(id -> id != null && !id.equals(customerId))
                .collect(Collectors.toList());
        
        if (!familyMemberIds.isEmpty()) {
            customerRepo.addFamilyMembers(customerId, familyMemberIds);
        }
    }

    @Override
    public List<CustomerResponseDTO> getAllCustomers() {
        return customerRepo.findAllCustomers().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    private void validateCustomer(CustomerDTO dto, Long id, boolean isUpdate) {
        if (dto.getName() == null || dto.getName().isEmpty()) {
            throw new RuntimeException("Name is mandatory.");
        }
        if (dto.getDob() == null) {
            throw new RuntimeException("Date of birth is mandatory.");
        }
        if (dto.getNic() == null || dto.getNic().isEmpty()) {
            throw new RuntimeException("NIC number is mandatory.");
        }

        if (isUpdate) {
            if (customerRepo.existsByNicAndIdNot(dto.getNic(), id)) {
                throw new RuntimeException("NIC number must be unique.");
            }
        } else {
            if (customerRepo.existsByNic(dto.getNic())) {
                throw new RuntimeException("NIC number must be unique.");
            }
        }
    }

    private CustomerResponseDTO convertToResponseDTO(Customer customer) {
        if (customer == null) return null;
        CustomerResponseDTO dto = new CustomerResponseDTO();
        dto.setId(customer.getId());
        dto.setName(customer.getName());
        dto.setDob(customer.getDob());
        dto.setNic(customer.getNic());

        if (customer.getMobileNumbers() != null && !customer.getMobileNumbers().isEmpty()) {
            dto.setMobileNumbers(customer.getMobileNumbers().stream()
                    .map(this::convertMobileToDTO)
                    .collect(Collectors.toList()));
        }

        if (customer.getAddresses() != null && !customer.getAddresses().isEmpty()) {
            dto.setAddresses(customer.getAddresses().stream()
                    .map(this::convertAddressToDTO)
                    .collect(Collectors.toList()));
        }

        if (customer.getFamilyMembers() != null && !customer.getFamilyMembers().isEmpty()) {
            dto.setFamilyMembers(customer.getFamilyMembers().stream()
                    .map(this::convertToResponseDTOBasic)
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    private CustomerResponseDTO convertToResponseDTOBasic(Customer customer) {
        if (customer == null) return null;
        CustomerResponseDTO dto = new CustomerResponseDTO();
        dto.setId(customer.getId());
        dto.setName(customer.getName());
        dto.setDob(customer.getDob());
        dto.setNic(customer.getNic());
        return dto;
    }

    private Customer convertToEntity(CustomerDTO dto) {
        if (dto == null) return null;
        Customer customer = new Customer();
        customer.setName(dto.getName());
        customer.setDob(dto.getDob());
        customer.setNic(dto.getNic());

        if (dto.getMobileNumbers() != null) {
            customer.setMobileNumbers(dto.getMobileNumbers().stream()
                    .map(this::convertMobileToEntity)
                    .collect(Collectors.toList()));
        }

        if (dto.getAddresses() != null) {
            customer.setAddresses(dto.getAddresses().stream()
                    .map(this::convertAddressToEntity)
                    .collect(Collectors.toList()));
        }

        if (dto.getFamilyMemberIds() != null) {
            customer.setFamilyMembers(dto.getFamilyMemberIds().stream()
                    .filter(id -> id != null && id > 0)
                    .map(id -> {
                        Customer fm = new Customer();
                        fm.setId(id);
                        return fm;
                    })
                    .collect(Collectors.toList()));
        }

        // Prevent self-referencing if id is known (for updates)
        if (customer.getId() != null && customer.getFamilyMembers() != null) {
            customer.setFamilyMembers(customer.getFamilyMembers().stream()
                    .filter(fm -> !fm.getId().equals(customer.getId()))
                    .collect(Collectors.toList()));
        }

        return customer;
    }

    private AddressResponseDTO convertAddressToDTO(Address address) {
        if (address == null) return null;
        AddressResponseDTO dto = new AddressResponseDTO();
        dto.setId(address.getId());
        dto.setAddressLine1(address.getAddressLine1());
        dto.setAddressLine2(address.getAddressLine2());
        dto.setCityID(address.getCityId());
        dto.setCityName(address.getCityName());
        dto.setCountryName(address.getCountryName());
        return dto;
    }

    private Address convertAddressToEntity(AddressDTO dto) {
        if (dto == null) return null;
        Address address = new Address();
        address.setAddressLine1(dto.getAddressLine1());
        address.setAddressLine2(dto.getAddressLine2());
        address.setCityId(dto.getCityID());
        // CityName and CountryName are usually fetched from DB, not set by user during create/update
        return address;
    }

    private CustomerMobileResponseDTO convertMobileToDTO(CustomerMobile mobile) {
        if (mobile == null) return null;
        CustomerMobileResponseDTO dto = new CustomerMobileResponseDTO();
        dto.setId(mobile.getId());
        dto.setMobileNumber(mobile.getMobileNo());
        return dto;
    }

    private CustomerMobile convertMobileToEntity(CustomerMobileDTO dto) {
        if (dto == null) return null;
        CustomerMobile mobile = new CustomerMobile();
        mobile.setMobileNo(dto.getMobileNumber());
        return mobile;
    }
}
