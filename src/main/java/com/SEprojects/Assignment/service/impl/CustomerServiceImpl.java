package com.SEprojects.Assignment.service.impl;

import com.SEprojects.Assignment.dto.AddressDTO;
import com.SEprojects.Assignment.dto.AddressResponseDTO;
import com.SEprojects.Assignment.dto.CustomerDTO;
import com.SEprojects.Assignment.dto.CustomerFamilyMemberDTO;
import com.SEprojects.Assignment.dto.CustomerMobileDTO;
import com.SEprojects.Assignment.dto.CustomerMobileResponseDTO;
import com.SEprojects.Assignment.dto.CustomerResponseDTO;
import com.SEprojects.Assignment.model.Address;
import com.SEprojects.Assignment.model.Customer;
import com.SEprojects.Assignment.model.CustomerMobile;
import com.SEprojects.Assignment.repo.CustomerRepo;
import com.SEprojects.Assignment.service.CustomerService;
import com.github.pjfanning.xlsx.StreamingReader;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
    public void addFamilyMembers(Long customerId, List<CustomerFamilyMemberDTO> familyMembers) {
        if (customerRepo.findCustomerById(customerId) == null) {
            throw new RuntimeException("Customer not found.");
        }
        List<Long> validIds = familyMembers.stream()
                .filter(fmDto -> fmDto != null && fmDto.getNic() != null && !fmDto.getNic().isEmpty())
                .map(fmDto -> customerRepo.findIdByNic(fmDto.getNic()))
                .filter(id -> id != null && !id.equals(customerId))
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

    @Override
    @Async
    public void processBulkFileAsync(File file) {
        try {
            processExcelFile(file);
        } finally {
            try {
                Files.deleteIfExists(file.toPath());
            } catch (IOException ignored) {}
        }
    }

    private void processExcelFile(File file) {
        try (InputStream is = new FileInputStream(file);
             Workbook workbook = StreamingReader.builder()
                     .rowCacheSize(100)
                     .bufferSize(4096)
                     .open(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            List<Customer> batch = new ArrayList<>();
            Map<String, Integer> cityMap = customerRepo.getCityNameToIdMap();
            
            int batchSize = 1000;
            boolean isHeader = true;

            for (Row r : sheet) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                String name = getCellValue(r.getCell(0));
                String dobStr = getCellValue(r.getCell(1));
                String nic = getCellValue(r.getCell(2));
                String mobileStr = getCellValue(r.getCell(3));
                String addr1 = getCellValue(r.getCell(4));
                String addr2 = getCellValue(r.getCell(5));
                String cityName = getCellValue(r.getCell(6));
                String countryName = getCellValue(r.getCell(7));

                if (name == null || dobStr == null || nic == null) continue;

                Customer customer = new Customer();
                customer.setName(name);
                customer.setNic(nic);
                
                try {
                    Cell dobCell = r.getCell(1);
                    if (dobCell.getCellType() == org.apache.poi.ss.usermodel.CellType.NUMERIC && org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(dobCell)) {
                        customer.setDob(dobCell.getDateCellValue());
                    } else {
                        customer.setDob(new java.text.SimpleDateFormat("yyyy-MM-dd").parse(dobStr));
                    }
                } catch (Exception e) {
                    continue;
                }

                // Parse mobiles
                if (mobileStr != null && !mobileStr.isEmpty()) {
                    String[] mobiles = mobileStr.split("[,;]");
                    for (String m : mobiles) {
                        CustomerMobile mobile = new CustomerMobile();
                        mobile.setMobileNo(m.trim());
                        customer.getMobileNumbers().add(mobile);
                    }
                }

                // Parse address
                if (addr1 != null && cityName != null && countryName != null) {
                    String cityKey = (cityName + "|" + countryName).toLowerCase();
                    Integer cityId = cityMap.get(cityKey);
                    if (cityId != null) {
                        Address addr = new Address();
                        addr.setAddressLine1(addr1);
                        addr.setAddressLine2(addr2);
                        addr.setCityId(cityId);
                        customer.getAddresses().add(addr);
                    }
                }

                batch.add(customer);

                if (batch.size() >= batchSize) {
                    processBatch(batch);
                    batch.clear();
                }
            }

            if (!batch.isEmpty()) {
                processBatch(batch);
            }

        } catch (Exception e) {
            System.err.println("Error processing excel file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void processBatch(List<Customer> customers) {
        customerRepo.saveCustomersBatch(customers);
        
        List<String> nics = customers.stream().map(Customer::getNic).collect(Collectors.toList());
        Map<String, Long> nicToId = customerRepo.getNicToIdMap(nics);
        
        List<Long> customerIds = new ArrayList<>(nicToId.values());
        customerRepo.deleteRelationsForIds(customerIds);
        
        List<CustomerMobile> allMobiles = new ArrayList<>();
        List<Address> allAddresses = new ArrayList<>();
        
        for (Customer c : customers) {
            Long id = nicToId.get(c.getNic());
            if (id != null) {
                for (CustomerMobile m : c.getMobileNumbers()) {
                    m.setCustomerId(id);
                    allMobiles.add(m);
                }
                for (Address a : c.getAddresses()) {
                    a.setCustomerId(id);
                    allAddresses.add(a);
                }
            }
        }
        
        customerRepo.batchSaveMobiles(allMobiles);
        customerRepo.batchSaveAddresses(allAddresses);
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return null;
        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue();
            case NUMERIC:
                if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                }
                return String.valueOf((long)cell.getNumericCellValue());
            case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
            default: return null;
        }
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

        if (dto.getFamilyMembers() != null) {
            customer.setFamilyMembers(dto.getFamilyMembers().stream()
                    .filter(fmDto -> fmDto != null && fmDto.getNic() != null && !fmDto.getNic().isEmpty())
                    .map(fmDto -> {
                        Long fmId = customerRepo.findIdByNic(fmDto.getNic());
                        if (fmId != null) {
                            Customer fm = new Customer();
                            fm.setId(fmId);
                            return fm;
                        }
                        return null;
                    })
                    .filter(fm -> fm != null)
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
