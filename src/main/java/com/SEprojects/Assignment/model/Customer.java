package com.SEprojects.Assignment.model;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Customer {
    private Long id;
    private String name;
    private Date dob;
    private List<CustomerMobile> mobileNumbers = new ArrayList<>();
    private List<Address> addresses = new ArrayList<>();
    private  List<Customer> familyMembers = new ArrayList<>();

    public Customer() {}

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

    public List<CustomerMobile> getMobileNumbers() {
        return mobileNumbers;
    }

    public void setMobileNumbers(List<CustomerMobile> mobileNumbers) {
        this.mobileNumbers = mobileNumbers;
    }

    public List<Address> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<Address> addresses) {
        this.addresses = addresses;
    }

    public List<Customer> getFamilyMembers() {
        return familyMembers;
    }

    public void setFamilyMembers(List<Customer> familyMembers) {
        this.familyMembers = familyMembers;
    }
}
