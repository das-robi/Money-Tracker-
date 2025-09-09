package com.devrobin.moneytracker.MVVM.Model;

public class InsightData {
    private final String text;
    private final String type; // "info", "warning", "success"

    public InsightData(String text, String type) {
        this.text = text;
        this.type = type;
    }

    // Getters
    public String getText() { return text; }
    public String getType() { return type; }
}

