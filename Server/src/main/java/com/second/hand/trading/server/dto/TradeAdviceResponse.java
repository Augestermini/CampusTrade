package com.second.hand.trading.server.dto;

import java.util.ArrayList;
import java.util.List;

public class TradeAdviceResponse {

    private String advice;
    private List<String> checkItems = new ArrayList<>();
    private List<String> questions = new ArrayList<>();
    private List<String> safetyTips = new ArrayList<>();
    private String riskLevel;

    public String getAdvice() {
        return advice;
    }

    public void setAdvice(String advice) {
        this.advice = advice;
    }

    public List<String> getCheckItems() {
        return checkItems;
    }

    public void setCheckItems(List<String> checkItems) {
        this.checkItems = checkItems;
    }

    public List<String> getQuestions() {
        return questions;
    }

    public void setQuestions(List<String> questions) {
        this.questions = questions;
    }

    public List<String> getSafetyTips() {
        return safetyTips;
    }

    public void setSafetyTips(List<String> safetyTips) {
        this.safetyTips = safetyTips;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }
}
