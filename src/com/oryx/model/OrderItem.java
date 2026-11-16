package com.oryx.model;

public class OrderItem {

    private String color;
    private int size;
    private int quantity;
    private double unitPrice;

    private int goodConditionQuantity;
    private int damagedQuantity;

    public OrderItem(String color, int size, int quantity, double unitPrice) {

        if (color == null) {
            throw new IllegalArgumentException("Color cannot be null.");
        }

        color = color.trim().toUpperCase();

        if (!color.equals("BLACK") &&
                !color.equals("WHITE") &&
                !color.equals("BLUE") &&
                !color.equals("BROWN")) {
            throw new IllegalArgumentException("Invalid color.");
        }

        if (size < 36 || size > 45) {
            throw new IllegalArgumentException("Invalid size.");
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException("Invalid quantity.");
        }

        if (unitPrice <= 0) {
            throw new IllegalArgumentException("Invalid unit price.");
        }

        this.color = color;
        this.size = size;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.goodConditionQuantity = 0;
        this.damagedQuantity = 0;
    }

    public void recordReturn(int goodQuantity, int damagedQuantity) {

        if (goodQuantity < 0 || damagedQuantity < 0) {
            throw new IllegalArgumentException("Return quantities cannot be negative.");
        }

        if (goodQuantity + damagedQuantity != quantity) {
            throw new IllegalArgumentException(
                    "Good and damaged quantities must equal the ordered quantity."
            );
        }

        if (isReturnProcessed()) {
            throw new IllegalArgumentException("This item has already been processed.");
        }

        this.goodConditionQuantity = goodQuantity;
        this.damagedQuantity = damagedQuantity;
    }

    public void restoreReturnDetails(int goodQuantity, int damagedQuantity) {

        if (goodQuantity < 0 || damagedQuantity < 0
                || goodQuantity + damagedQuantity > quantity) {
            throw new IllegalArgumentException("Invalid saved return quantities.");
        }

        this.goodConditionQuantity = goodQuantity;
        this.damagedQuantity = damagedQuantity;
    }

    public boolean isReturnProcessed() {
        return goodConditionQuantity + damagedQuantity == quantity;
    }

    public String getColor() { return color; }
    public int getSize() { return size; }
    public int getQuantity() { return quantity; }
    public double getUnitPrice() { return unitPrice; }
    public int getGoodConditionQuantity() { return goodConditionQuantity; }
    public int getDamagedQuantity() { return damagedQuantity; }

    public double getSubtotal() {
        return quantity * unitPrice;
    }

    public double getDamagedLoss() {
        return damagedQuantity * unitPrice;
    }
}
