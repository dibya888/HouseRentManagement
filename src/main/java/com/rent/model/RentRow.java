package com.rent.model;

public class RentRow {
    private int id;
    private int tenantId;

    private String flatNo;
    private String meterNo;

    private String tenantName;
    private String phone;

    private String billMonth;     // YYYY-MM
    private double houseRent;

    private double electricity;
    private double water;
    private double gas;

    private double otherBills;
    private double fine;
    private double discount;

    private double total;

    private String status;        // DUE/PARTIAL/LATE
    private String dueDate;       // YYYY-MM-DD
    private String paymentDate;   // YYYY-MM-DD (nullable)
    private double paidAmount;
    private String notes;
    private String receiptNo;

    public RentRow() {}

    // getters/setters (generate in IDE)
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getTenantId() { return tenantId; }
    public void setTenantId(int tenantId) { this.tenantId = tenantId; }

    public String getFlatNo() { return flatNo; }
    public void setFlatNo(String flatNo) { this.flatNo = flatNo; }

    public String getMeterNo() { return meterNo; }
    public void setMeterNo(String meterNo) { this.meterNo = meterNo; }

    public String getTenantName() { return tenantName; }
    public void setTenantName(String tenantName) { this.tenantName = tenantName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getBillMonth() { return billMonth; }
    public void setBillMonth(String billMonth) { this.billMonth = billMonth; }

    public double getHouseRent() { return houseRent; }
    public void setHouseRent(double houseRent) { this.houseRent = houseRent; }

    public double getElectricity() { return electricity; }
    public void setElectricity(double electricity) { this.electricity = electricity; }

    public double getWater() { return water; }
    public void setWater(double water) { this.water = water; }

    public double getGas() { return gas; }
    public void setGas(double gas) { this.gas = gas; }

    public double getOtherBills() { return otherBills; }
    public void setOtherBills(double otherBills) { this.otherBills = otherBills; }

    public double getFine() { return fine; }
    public void setFine(double fine) { this.fine = fine; }

    public double getDiscount() { return discount; }
    public void setDiscount(double discount) { this.discount = discount; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    /**
     * Display-only derived status: if the stored status is still 'DUE'
     * (meaning no payment action has ever been taken on this row) but
     * the due date has already passed, this returns 'LATE' instead —
     * without modifying the underlying stored status or the database.
     * All other statuses (PARTIAL, LATE, PAID) are returned unchanged,
     * since those are already set correctly by RentDAO.applyPayment()
     * at the moment a payment action occurs.
     */
    public String getDisplayStatus() {
        if ("DUE".equalsIgnoreCase(status) && dueDate != null && !dueDate.isBlank()) {
            try {
                java.time.LocalDate due = java.time.LocalDate.parse(dueDate);
                if (java.time.LocalDate.now().isAfter(due)) {
                    return "LATE";
                }
            } catch (Exception e) {
                // Malformed due date — fall through and show the stored status as-is.
            }
        }
        return status;
    }

    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }

    public String getPaymentDate() { return paymentDate; }
    public void setPaymentDate(String paymentDate) { this.paymentDate = paymentDate; }

    public double getPaidAmount() { return paidAmount; }
    public void setPaidAmount(double paidAmount) { this.paidAmount = paidAmount; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getReceiptNo() {
        return receiptNo;
    }

    public void setReceiptNo(String receiptNo) {
        this.receiptNo = receiptNo;
    }
}