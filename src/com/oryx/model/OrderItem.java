package com.oryx.model;

import com.oryx.validation.OrderItemValidator;

public class OrderItem {

    private final String color;
    private final int size;
    private final int quantity;

    public OrderItem(String color, int size, int quantity) {

        // Validate all inputs before creating the object
        this.color = OrderItemValidator.validateColor(color);

        OrderItemValidator.validateSize(size);

        OrderItemValidator.validateQuantity(quantity);

        this.size = size;
        this.quantity = quantity;
    }

    public String getColor() {
        return color;
    }

    public int getSize() {
        return size;
    }

    public int getQuantity() {
        return quantity;
    }
}
