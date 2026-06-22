package com.second.hand.trading.server.dto;

public class GenerateDescriptionResponse {

    private String description;

    public GenerateDescriptionResponse() {
    }

    public GenerateDescriptionResponse(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
