package com.oryx.service;

import com.oryx.model.Order;
import com.oryx.model.OrderItem;
import com.oryx.model.OrderStatus;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ReturnService {

    private final Inventory inventory;
    private final Set<Integer> processedOrderIds;

    public ReturnService(Inventory inventory) {

        if (inventory == null) {
            throw new IllegalArgumentException("Inventory cannot be null.");
        }

        this.inventory = inventory;
        this.processedOrderIds = new HashSet<>();
    }

    public void processReturnedOrder(
            Order order,
            List<Integer> goodQuantities,
            List<Integer> damagedQuantities) {

        if (order == null || goodQuantities == null || damagedQuantities == null) {
            throw new IllegalArgumentException("Return details cannot be null.");
        }

        if (order.getStatus() != OrderStatus.RETURNED) {
            throw new IllegalArgumentException("Order is not returned.");
        }

        if (processedOrderIds.contains(order.getOrderId())) {
            throw new IllegalArgumentException("This order has already been processed.");
        }

        List<OrderItem> items = order.getItems();

        if (goodQuantities.size() != items.size()
                || damagedQuantities.size() != items.size()) {
            throw new IllegalArgumentException("Return quantities do not match order items.");
        }

        // Validate all quantities before changing stock.
        for (int i = 0; i < items.size(); i++) {
            int good = goodQuantities.get(i);
            int damaged = damagedQuantities.get(i);

            if (good < 0 || damaged < 0
                    || good + damaged != items.get(i).getQuantity()) {
                throw new IllegalArgumentException(
                        "Good + Damaged quantity must equal ordered quantity for every item."
                );
            }

            if (items.get(i).isReturnProcessed()) {
                throw new IllegalArgumentException("An item has already been processed.");
            }
        }

        // Add only good-condition shoes to the matching color and size.
        for (int i = 0; i < items.size(); i++) {
            OrderItem item = items.get(i);
            int good = goodQuantities.get(i);
            int damaged = damagedQuantities.get(i);

            item.recordReturn(good, damaged);

            if (good > 0) {
                inventory.addStock(item.getColor(), item.getSize(), good);
            }
        }

        processedOrderIds.add(order.getOrderId());

        System.out.println("Returned order processed successfully!");
    }

    // Keep compatibility with orders restocked by the previous system.
    public void loadRestockedOrderIds(List<Integer> orderIds, List<Order> orders) {

        if (orderIds == null || orders == null) {
            return;
        }

        for (int orderId : orderIds) {
            processedOrderIds.add(orderId);

            for (Order order : orders) {
                if (order.getOrderId() == orderId) {
                    for (OrderItem item : order.getItems()) {
                        if (!item.isReturnProcessed()) {
                            item.restoreReturnDetails(item.getQuantity(), 0);
                        }
                    }
                    break;
                }
            }
        }
    }

    public List<Integer> getRestockedOrderIds() {
        return new ArrayList<>(processedOrderIds);
    }

    public boolean isOrderProcessed(int orderId) {
        return processedOrderIds.contains(orderId);
    }
}
