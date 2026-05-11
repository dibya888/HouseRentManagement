package com.rent.model;

public class Flat {

    private String flatNo;
    private int bedrooms;
    private int bathrooms;
    private int kitchens;
    private int balconies;
    private int diningrooms;
    private int livingrooms;
    private double rent;
    private String status; // Available / Occupied

    public Flat(String flatNo,
                int bedrooms,
                int bathrooms,
                int kitchens,
                int balconies,
                int diningrooms,
                int livingrooms,
                double rent,
                String status) {

        this.flatNo = flatNo;
        this.bedrooms = bedrooms;
        this.bathrooms = bathrooms;
        this.kitchens = kitchens;
        this.balconies = balconies;
        this.diningrooms = diningrooms;
        this.livingrooms = livingrooms;
        this.rent = rent;
        this.status = status;
    }

    public String getFlatNo() {
        return flatNo;
    }

    public int getBedrooms() {
        return bedrooms;
    }

    public int getBathrooms() {
        return bathrooms;
    }

    public int getKitchens() {
        return kitchens;
    }
    public int getBalconies() {
        return balconies;
    }
    public int getDiningrooms() {
        return diningrooms;
    }
    public int getLivingrooms() {
        return livingrooms;
    }

    public double getRent() {
        return rent;
    }

    public String getStatus() {
        return status;
    }
}