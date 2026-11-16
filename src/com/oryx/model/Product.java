
package com.oryx.model;

public class Product {

    // Attributes
    private int productId;
    private String productName;
    private double price;

    // Constructor
    public Product(int productId, String productName, double price) {

        this.productId = productId;
        this.productName = productName;
        this.price = price;

    }

    // Getter for productId
    public int getProductId() {
        return productId;
    }

    // Getter for productName
    public String getProductName() {
        return productName;
    }

    // Getter for price
    public double getPrice() {
        return price;
    }

    // Method to update price
    public void updatePrice(double newPrice) {

        if (newPrice > 0) {
            this.price = newPrice;
            System.out.println("Product price updated successfully!");
        } else {
            System.out.println("Invalid price!");
        }

    }

}
