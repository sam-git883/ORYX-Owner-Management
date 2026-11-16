
package com.oryx.service;

import com.oryx.model.Order;
import com.oryx.model.OrderItem;
import com.oryx.model.OrderStatus;

import java.util.List;

public class OrderService {

    private final Inventory inventory;

    // Constructor
    public OrderService(Inventory inventory) {

        if (inventory == null) {
            throw new IllegalArgumentException("Inventory cannot be null.");
        }

        this.inventory = inventory;
    }

    // Process an order
    public void processOrder(Order order) {

        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null.");
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalArgumentException("Order is not pending.");
        }

        List<OrderItem> items = order.getItems();

        if (items.isEmpty()) {
            throw new IllegalArgumentException("Order has no items.");
        }

        // Check stock availability
        for (OrderItem item : items) {

            if (!inventory.hasStock(
                    item.getColor(),
                    item.getSize(),
                    item.getQuantity()
            )) {
                throw new IllegalArgumentException(
                        "Insufficient stock for "
                                + item.getColor() + "-" + item.getSize()
                );
            }
        }

        // Reduce stock
        for (OrderItem item : items) {

            inventory.reduceStock(
                    item.getColor(),
                    item.getSize(),
                    item.getQuantity()
            );
        }

        // Update order status
        order.updateStatus(OrderStatus.DISPATCHED);

        System.out.println("Order processed successfully!");
    }
}
