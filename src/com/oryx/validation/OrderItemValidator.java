package com.oryx.validation;

public class OrderItemValidator {

    public static String validateColor(String color) {

        if (color == null || color.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Color cannot be empty."
            );
        }

        String validColor = color.trim().toLowerCase();

        switch (validColor) {
            case "black":
                return "Black";
            case "white":
                return "White";
            case "blue":
                return "Blue";
            case "brown":
                return "Brown";
            default:
                throw new IllegalArgumentException(
                        "Invalid color. Choose Black, White, Blue or Brown."
                );
        }
    }

    public static void validateSize(int size) {

        if (size < 36 || size > 45) {
            throw new IllegalArgumentException(
                    "Invalid shoe size. Choose between 36 and 45."
            );
        }
    }

    public static void validateQuantity(int quantity) {

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than zero."
            );
        }
    }
}