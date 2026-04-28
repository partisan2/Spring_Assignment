package com.SEprojects.Assignment.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CustomerResponseDTO {
    private Long id;
    private String name;
    private Date dob;
    private String nic;
    private List<CustomerMobileResponseDTO> mobileNumbers = new ArrayList<>();
    private List<AddressResponseDTO> addresses = new ArrayList<>();
    private List<CustomerResponseDTO> familyMembers = new ArrayList<>();

    // Default constructor
    public CustomerResponseDTO() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDob() {
        return dob;
    }

    public void setDob(Date dob) {
        this.dob = dob;
    }

    public String getNic() {
        return nic;
    }

    public void setNic(String nic) {
        this.nic = nic;
    }

    public List<CustomerMobileResponseDTO> getMobileNumbers() {
        return mobileNumbers;
    }

    public void setMobileNumbers(List<CustomerMobileResponseDTO> mobileNumbers) {
        this.mobileNumbers = mobileNumbers;
    }

    public List<AddressResponseDTO> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<AddressResponseDTO> addresses) {
        this.addresses = addresses;
    }

    public List<CustomerResponseDTO> getFamilyMembers() {
        return familyMembers;
    }

    public void setFamilyMembers(List<CustomerResponseDTO> familyMembers) {
        this.familyMembers = familyMembers;
    }
}
