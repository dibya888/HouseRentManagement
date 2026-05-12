package com.rent.model;

public class ChartItem {

    private String label;
    private double value;

    public ChartItem(String label, double value) {
        this.label = label;
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public double getValue() {
        return value;
    }
}