package com.SEprojects.Assignment.dto;

public class CustomerMobileResponseDTO {
    private Long id;
    private String mobileNumber;

    public CustomerMobileResponseDTO() {
    }

    public CustomerMobileResponseDTO(Long id, String mobileNumber) {
        this.id = id;
        this.mobileNumber = mobileNumber;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }
}
