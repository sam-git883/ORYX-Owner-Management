package com.oryx.reports;

import com.oryx.model.Order;
import com.oryx.model.OrderItem;
import com.oryx.model.OrderStatus;

import java.util.List;

public class ColorReport extends Report {

    @Override
    public void generateReport(List<Order> orders) {

        String[] colors = {"BLACK", "WHITE", "BLUE", "BROWN"};
        int highestQuantity = 0;
        String bestColor = "";
        int totalSold = 0;

        System.out.println("===== COLOR SALES =====");
        System.out.printf("%-12s %s%n", "Color", "Units Sold");
        System.out.println("------------------------------");

        for (String color : colors) {
            int quantity = 0;

            for (Order order : orders) {
                if (order.getStatus() != OrderStatus.COMPLETED) {
                    continue;
                }

                for (OrderItem item : order.getItems()) {
                    if (item.getColor().equalsIgnoreCase(color)) {
                        quantity += item.getQuantity();
                    }
                }
            }

            // Hide colors with no sales to keep the report concise.
            if (quantity > 0) {
                System.out.printf("%-12s %d%n", color, quantity);
            }

            totalSold += quantity;

            if (quantity > highestQuantity) {
                highestQuantity = quantity;
                bestColor = color;
            }
        }

        System.out.println("------------------------------");
        System.out.println("Total Units Sold: " + totalSold);
        if (!bestColor.isEmpty()) {
            System.out.println(
                    "Best Selling Color: " + bestColor
                            + " (" + highestQuantity + " units)"
            );
        } else {
            System.out.println("Best Selling Color: No sales");
        }
    }
}
