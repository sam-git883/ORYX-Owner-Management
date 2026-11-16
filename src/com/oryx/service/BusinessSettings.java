package com.oryx.service;

/**
 * Stores the current business prices used when creating NEW orders.
 * Each Order keeps its own snapshot, so changing these settings never
 * changes the financial values of an existing order.
 */
public class BusinessSettings {

    public static final double DEFAULT_SHOE_PRICE = 8500.00;
    public static final double DEFAULT_PROFIT_PER_SHOE = 1300.00;
    public static final double DEFAULT_DELIVERY_CHARGE = 450.00;

    private double shoePrice;
    private double profitPerShoe;
    private double deliveryCharge;

    public BusinessSettings() {
        this(DEFAULT_SHOE_PRICE, DEFAULT_PROFIT_PER_SHOE, DEFAULT_DELIVERY_CHARGE);
    }

    public BusinessSettings(double shoePrice, double profitPerShoe, double deliveryCharge) {
        validateShoePrice(shoePrice);
        validateNonNegative(profitPerShoe, "Profit per shoe");
        validateNonNegative(deliveryCharge, "Delivery charge");
        this.shoePrice = shoePrice;
        this.profitPerShoe = profitPerShoe;
        this.deliveryCharge = deliveryCharge;
    }

    public double getShoePrice() { return shoePrice; }
    public double getProfitPerShoe() { return profitPerShoe; }
    public double getDeliveryCharge() { return deliveryCharge; }

    public void setShoePrice(double value) {
        validateShoePrice(value);
        shoePrice = value;
    }

    public void setProfitPerShoe(double value) {
        validateNonNegative(value, "Profit per shoe");
        profitPerShoe = value;
    }

    public void setDeliveryCharge(double value) {
        validateNonNegative(value, "Delivery charge");
        deliveryCharge = value;
    }

    private static void validateShoePrice(double value) {
        if (!Double.isFinite(value) || value <= 0) {
            throw new IllegalArgumentException("Shoe price must be greater than zero.");
        }
    }

    private static void validateNonNegative(double value, String fieldName) {
        if (!Double.isFinite(value) || value < 0) {
            throw new IllegalArgumentException(fieldName + " cannot be negative.");
        }
    }
}
