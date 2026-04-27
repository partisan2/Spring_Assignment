package com.SEprojects.Assignment.dto;

public class AddressDTO {
    private Long id;
    private Long customerId;
    private String addressLine1;
    private String addressLine2;
    private int cityID;
    private String cityName;
    private String countryName;

    public AddressDTO() {
    }

    public AddressDTO(Long id, Long customerId, String addressLine1, String addressLine2, int cityID, String cityName, String countryName) {
        this.id = id;
        this.customerId = customerId;
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.cityID = cityID;
        this.cityName = cityName;
        this.countryName = countryName;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
    }

    public int getCityID() {
        return cityID;
    }

    public void setCityID(int cityID) {
        this.cityID = cityID;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }
}
