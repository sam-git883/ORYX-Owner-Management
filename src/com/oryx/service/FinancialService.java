package com.oryx.service;

import com.oryx.model.Order;
import com.oryx.model.OrderItem;
import com.oryx.model.OrderStatus;

import java.util.List;
import java.time.LocalDate;

public class FinancialService {

    public double calculateTotalProfit(List<Order> orders) {
        return calculateTotalProfit(orders, null, null);
    }

    public double calculateTotalProfit(
            List<Order> orders, LocalDate startDate, LocalDate endDate) {

        double totalProfit = 0;

        for (Order order : orders) {
            LocalDate date = order.getCompletedDate();
            if (order.getStatus() == OrderStatus.COMPLETED
                    && isWithinPeriod(date, startDate, endDate)) {
                totalProfit += order.getTotalQuantity() * order.getProfitPerShoe();
            }
        }

        return totalProfit;
    }

    public double calculateDeliveryLoss(List<Order> orders) {
        return calculateDeliveryLoss(orders, null, null);
    }

    public double calculateDeliveryLoss(
            List<Order> orders, LocalDate startDate, LocalDate endDate) {

        double totalLoss = 0;

        for (Order order : orders) {
            if (order.getStatus() == OrderStatus.RETURNED
                    && isWithinPeriod(order.getReturnedDate(), startDate, endDate)) {
                totalLoss += order.getDeliveryCharge();
            }
        }

        return totalLoss;
    }

    public double calculateDamagedProductLoss(List<Order> orders) {
        return calculateDamagedProductLoss(orders, null, null);
    }

    public double calculateDamagedProductLoss(
            List<Order> orders, LocalDate startDate, LocalDate endDate) {

        double totalLoss = 0;

        for (Order order : orders) {
            if (order.getStatus() == OrderStatus.RETURNED
                    && isWithinPeriod(order.getReturnedDate(), startDate, endDate)) {
                for (OrderItem item : order.getItems()) {
                    totalLoss += item.getDamagedLoss();
                }
            }
        }

        return totalLoss;
    }

    public double calculateTotalLoss(List<Order> orders) {
        return calculateTotalLoss(orders, null, null);
    }

    public double calculateTotalLoss(
            List<Order> orders, LocalDate startDate, LocalDate endDate) {
        return calculateDeliveryLoss(orders, startDate, endDate)
                + calculateDamagedProductLoss(orders, startDate, endDate);
    }

    public double calculateNetProfit(List<Order> orders) {
        return calculateNetProfit(orders, null, null);
    }

    public double calculateNetProfit(
            List<Order> orders, LocalDate startDate, LocalDate endDate) {
        return calculateTotalProfit(orders, startDate, endDate)
                - calculateTotalLoss(orders, startDate, endDate);
    }

    private boolean isWithinPeriod(
            LocalDate date, LocalDate startDate, LocalDate endDate) {
        // Null dates belong to All Time only (both bounds are null).
        if (startDate == null && endDate == null) {
            return true;
        }
        return date != null
                && !date.isBefore(startDate)
                && !date.isAfter(endDate);
    }
}
