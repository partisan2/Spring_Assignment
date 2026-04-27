package com.SEprojects.Assignment.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CustomerDTO {
    private Long id;
    private String name;
    private Date dob;
    private String nic;
    private List<CustomerMobileDTO> mobileNumbers = new ArrayList<>();
    private List<AddressDTO> addresses = new ArrayList<>();
    private List<CustomerDTO> familyMembers = new ArrayList<>();

    public CustomerDTO() {
    }

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

    public List<CustomerMobileDTO> getMobileNumbers() {
        return mobileNumbers;
    }

    public void setMobileNumbers(List<CustomerMobileDTO> mobileNumbers) {
        this.mobileNumbers = mobileNumbers;
    }

    public List<AddressDTO> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<AddressDTO> addresses) {
        this.addresses = addresses;
    }

    public List<CustomerDTO> getFamilyMembers() {
        return familyMembers;
    }

    public void setFamilyMembers(List<CustomerDTO> familyMembers) {
        this.familyMembers = familyMembers;
    }
}
