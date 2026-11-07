package com.oryx.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Order {

    private final String orderNumber;
    private final String customerName;
    private final String district;
    private final List<OrderItem> items;

    private OrderStatus status;

    public Order(String orderNumber, String customerName,
                 String district, List<OrderItem> items) {

        if (orderNumber == null || orderNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Order number cannot be empty.");
        }

        if (customerName == null || customerName.trim().isEmpty()) {
            throw new IllegalArgumentException("Customer name cannot be empty.");
        }

        if (district == null || district.trim().isEmpty()) {
            throw new IllegalArgumentException("District cannot be empty.");
        }

        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item.");
        }

        this.orderNumber = orderNumber.trim();
        this.customerName = customerName.trim();
        this.district = district.trim();

        this.items = new ArrayList<>(items);

        this.status = OrderStatus.PENDING;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getDistrict() {
        return district;
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {

        if (status == null) {
            throw new IllegalArgumentException("Order status cannot be null.");
        }

        this.status = status;
    }
}