package com.second.hand.trading.server.dto;

import java.math.BigDecimal;

public class GenerateDescriptionRequest {

    private String name;
    private String category;
    private String conditionLevel;
    private String usedTime;
    private String accessories;
    private BigDecimal price;
    private Boolean campusTrade;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getConditionLevel() {
        return conditionLevel;
    }

    public void setConditionLevel(String conditionLevel) {
        this.conditionLevel = conditionLevel;
    }

    public String getUsedTime() {
        return usedTime;
    }

    public void setUsedTime(String usedTime) {
        this.usedTime = usedTime;
    }

    public String getAccessories() {
        return accessories;
    }

    public void setAccessories(String accessories) {
        this.accessories = accessories;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Boolean getCampusTrade() {
        return campusTrade;
    }

    public void setCampusTrade(Boolean campusTrade) {
        this.campusTrade = campusTrade;
    }
}
