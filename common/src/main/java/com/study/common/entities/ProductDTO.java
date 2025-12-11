package com.study.common.entities;


public class ProductDTO {
    private String name;
    private Integer quantity;
    private String material;
    private Double cost;
    private String image;
    private Gender gender;
    private ClothesType clothesType;


    public ProductDTO(String name, Integer quantity, String material, Double cost,  String image,  Gender gender, ClothesType clothesType) {
        this.name = name;
        this.quantity = quantity;
        this.material = material;
        this.cost = cost;
        this.image = image;
        this.gender = gender;
        this.clothesType = clothesType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Double getCost() {
        return cost;
    }

    public void setCost(Double cost) {
        this.cost = cost;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public ClothesType getClothesType() {
        return clothesType;
    }

    public void setClothesType(ClothesType clothesType) {
        this.clothesType = clothesType;
    }
}
