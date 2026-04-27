package com.SEprojects.Assignment.dto;

public class CountyDTO {
    private int id;
    private String name;

    public CountyDTO() {
    }

    public CountyDTO(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
