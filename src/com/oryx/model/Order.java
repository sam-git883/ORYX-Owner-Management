
package com.oryx.model;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;

public class Order {

    private int orderId;
    private String customerName;
    private String address;
    private String district;
    private String phoneNumber;

    private List<OrderItem> items;
    private OrderStatus status;
    private LocalDate orderDate;
    private LocalDate completedDate;
    private LocalDate returnedDate;

    // Financial values captured when this order is created.
    private double profitPerShoe;
    private double deliveryCharge;

    // Constructor for existing sample orders
    public Order(int orderId, String customerName, String district) {

        if (orderId <= 0) {
            throw new IllegalArgumentException("Invalid order ID.");
        }

        if (customerName == null || customerName.isBlank()) {
            throw new IllegalArgumentException("Invalid customer name.");
        }

        if (district == null || district.isBlank()) {
            throw new IllegalArgumentException("Invalid district.");
        }

        this.orderId = orderId;
        this.customerName = customerName.trim();
        this.address = "Not provided";
        this.district = district.trim();
        this.phoneNumber = "Not provided";

        this.items = new ArrayList<>();
        this.status = OrderStatus.PENDING;
        this.orderDate = LocalDate.now();
        this.profitPerShoe = 1300.00;
        this.deliveryCharge = 450.00;
    }

    // Constructor for new customer orders
    public Order(
            int orderId,
            String customerName,
            String address,
            String district,
            String phoneNumber) {
        this(orderId, customerName, address, district, phoneNumber, 1300.00, 450.00);
    }

    // Constructor for new orders with the current business settings snapshot.
    public Order(int orderId, String customerName, String address, String district,
                 String phoneNumber, double profitPerShoe, double deliveryCharge) {

        if (orderId <= 0) throw new IllegalArgumentException("Invalid order ID.");
        if (customerName == null || customerName.isBlank()) throw new IllegalArgumentException("Customer name is required.");
        if (address == null || address.isBlank()) throw new IllegalArgumentException("Address is required.");
        if (district == null || district.isBlank()) throw new IllegalArgumentException("District is required.");
        if (phoneNumber == null || !phoneNumber.matches("0\\d{9}")) throw new IllegalArgumentException("Invalid phone number. Enter a 10-digit number starting with 0.");
        if (!Double.isFinite(profitPerShoe) || profitPerShoe < 0) throw new IllegalArgumentException("Invalid profit per shoe.");
        if (!Double.isFinite(deliveryCharge) || deliveryCharge < 0) throw new IllegalArgumentException("Invalid delivery charge.");

        this.orderId = orderId;
        this.customerName = customerName.trim();
        this.address = address.trim();
        this.district = district.trim();
        this.phoneNumber = phoneNumber.trim();
        this.items = new ArrayList<>();
        this.status = OrderStatus.PENDING;
        this.orderDate = LocalDate.now();
        this.profitPerShoe = profitPerShoe;
        this.deliveryCharge = deliveryCharge;
    }

    public void addItem(OrderItem item) {

        if (item == null) {
            throw new IllegalArgumentException("Order item cannot be null.");
        }

        for (OrderItem existingItem : items) {

            if (existingItem.getColor().equals(item.getColor())
                    && existingItem.getSize() == item.getSize()) {

                throw new IllegalArgumentException(
                        "This shoe variant already exists in the order."
                );
            }
        }

        items.add(item);
    }

    public int getTotalQuantity() {

        int total = 0;

        for (OrderItem item : items) {
            total += item.getQuantity();
        }

        return total;
    }

    public double getTotalAmount() {

        double total = 0;

        for (OrderItem item : items) {
            total += item.getSubtotal();
        }

        return total;
    }

    public void updateStatus(OrderStatus newStatus) {

        if (newStatus == null) {
            throw new IllegalArgumentException("Status cannot be null.");
        }

        if (status == OrderStatus.PENDING
                && newStatus == OrderStatus.DISPATCHED) {

            status = newStatus;

        } else if (status == OrderStatus.DISPATCHED
                && (newStatus == OrderStatus.COMPLETED
                || newStatus == OrderStatus.RETURNED)) {

            status = newStatus;

            if (newStatus == OrderStatus.COMPLETED) {
                completedDate = LocalDate.now();
            } else {
                returnedDate = LocalDate.now();
            }

        } else {

            throw new IllegalArgumentException(
                    "Invalid order status transition."
            );
        }
    }


    public LocalDate getOrderDate() {
        return orderDate;
    }

    public LocalDate getCompletedDate() {
        return completedDate;
    }

    public LocalDate getReturnedDate() {
        return returnedDate;
    }

    // Restore dates saved in CSV. Null means this is an older order
    // created before date tracking was introduced.
    public void restoreDates(
            LocalDate savedOrderDate,
            LocalDate savedCompletedDate,
            LocalDate savedReturnedDate) {

        this.orderDate = savedOrderDate;
        this.completedDate = savedCompletedDate;
        this.returnedDate = savedReturnedDate;
    }

    public double getProfitPerShoe() { return profitPerShoe; }
    public double getDeliveryCharge() { return deliveryCharge; }

    public void restoreFinancialSettings(double savedProfitPerShoe, double savedDeliveryCharge) {
        if (!Double.isFinite(savedProfitPerShoe) || savedProfitPerShoe < 0
                || !Double.isFinite(savedDeliveryCharge) || savedDeliveryCharge < 0) {
            throw new IllegalArgumentException("Invalid saved order financial settings.");
        }
        this.profitPerShoe = savedProfitPerShoe;
        this.deliveryCharge = savedDeliveryCharge;
    }

    public int getOrderId() {
        return orderId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getAddress() {
        return address;
    }

    public String getDistrict() {
        return district;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public List<OrderItem> getItems() {
        return new ArrayList<>(items);
    }

    public OrderStatus getStatus() {
        return status;
    }
}
