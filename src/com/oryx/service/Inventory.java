
package com.oryx.service;

import java.util.HashMap;
import java.util.Map;

public class Inventory {

    private final Map<String, Integer> stock;

    public Inventory() {
        stock = new HashMap<>();
    }

    // Validate color and size
    private void validateVariant(String color, int size) {

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
    }

    // Add stock
    public void addStock(String color, int size, int quantity) {

        validateVariant(color, size);

        if (quantity <= 0) {
            throw new IllegalArgumentException("Invalid quantity.");
        }

        String key = color.trim().toUpperCase() + "-" + size;

        int currentStock = stock.getOrDefault(key, 0);

        stock.put(key, currentStock + quantity);
    }

    // Get stock
    public int getStock(String color, int size) {

        validateVariant(color, size);

        String key = color.trim().toUpperCase() + "-" + size;

        return stock.getOrDefault(key, 0);
    }

    // Check stock availability
    public boolean hasStock(String color, int size, int quantity) {

        if (quantity <= 0) {
            return false;
        }

        return getStock(color, size) >= quantity;
    }

    // Reduce stock
    public void reduceStock(String color, int size, int quantity) {

        validateVariant(color, size);

        if (quantity <= 0) {
            throw new IllegalArgumentException("Invalid quantity.");
        }

        String key = color.trim().toUpperCase() + "-" + size;

        int currentStock = stock.getOrDefault(key, 0);

        if (currentStock < quantity) {
            throw new IllegalArgumentException("Insufficient stock.");
        }

        stock.put(key, currentStock - quantity);
    }

    // Get all inventory stock
    public Map<String, Integer> getAllStock() {
        return new HashMap<>(stock);
    }
}
