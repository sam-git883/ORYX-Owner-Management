package com.oryx;

import com.oryx.model.OrderItem;

public class Main {

    public static void main(String[] args) {

        System.out.println("===== ORYX VALIDATION TEST =====");

        // Test 1: Valid order item
        try {
            OrderItem item = new OrderItem("Black", 36, 2);

            System.out.println("\nTest 1: Valid Item");
            System.out.println("Color: " + item.getColor());
            System.out.println("Size: " + item.getSize());
            System.out.println("Quantity: " + item.getQuantity());

        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }

        // Test 2: Invalid color
        try {
            new OrderItem("Pink", 36, 2);

        } catch (IllegalArgumentException e) {
            System.out.println("\nTest 2: Invalid Color");
            System.out.println(e.getMessage());
        }

        // Test 3: Invalid size
        try {
            new OrderItem("Black", 50, 2);

        } catch (IllegalArgumentException e) {
            System.out.println("\nTest 3: Invalid Size");
            System.out.println(e.getMessage());
        }

        // Test 4: Invalid quantity
        try {
            new OrderItem("White", 38, 0);

        } catch (IllegalArgumentException e) {
            System.out.println("\nTest 4: Invalid Quantity");
            System.out.println(e.getMessage());
        }
    }
}
