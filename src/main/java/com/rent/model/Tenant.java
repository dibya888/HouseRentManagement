package com.rent.model;

public class Tenant {

    private int id;
    private String name;
    private String phone;
    private String email;
    private String nid;

    private String address;   // Permanent Address
    private String flatNo;
    private double rent;

    private String nidPath;
    private String docPath;

    public Tenant() {}

    public Tenant(int id, String name, String phone, String email, String nid,
                  String address, String flatNo, double rent,
                  String nidPath, String docPath) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.nid = nid;
        this.address = address;
        this.flatNo = flatNo;
        this.rent = rent;
        this.nidPath = nidPath;
        this.docPath = docPath;
    }

    public Tenant(String name, String phone, String email, String nid,
                  String address, String flatNo, double rent,
                  String nidPath, String docPath) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.nid = nid;
        this.address = address;
        this.flatNo = flatNo;
        this.rent = rent;
        this.nidPath = nidPath;
        this.docPath = docPath;
    }

    // getters/setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNid() { return nid; }
    public void setNid(String nid) { this.nid = nid; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getFlatNo() { return flatNo; }
    public void setFlatNo(String flatNo) { this.flatNo = flatNo; }

    public double getRent() { return rent; }
    public void setRent(double rent) { this.rent = rent; }

    public String getNidPath() { return nidPath; }
    public void setNidPath(String nidPath) { this.nidPath = nidPath; }

    public String getDocPath() { return docPath; }
    public void setDocPath(String docPath) { this.docPath = docPath; }
}