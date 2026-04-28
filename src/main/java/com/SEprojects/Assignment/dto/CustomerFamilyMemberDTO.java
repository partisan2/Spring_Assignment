package com.SEprojects.Assignment.dto;

public class CustomerFamilyMemberDTO {
    private String nic;

    public CustomerFamilyMemberDTO() {
    }

    public CustomerFamilyMemberDTO(String nic) {
        this.nic = nic;
    }

    public String getNic() {
        return nic;
    }

    public void setNic(String nic) {
        this.nic = nic;
    }
}
