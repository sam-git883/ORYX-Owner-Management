package com.oryx.reports;

import com.oryx.model.Order;
import com.oryx.model.OrderItem;
import com.oryx.model.OrderStatus;

import java.util.List;

public class SizeReport extends Report {

    @Override
    public void generateReport(List<Order> orders) {

        int highestQuantity = 0;
        int bestSize = 0;
        int totalSold = 0;

        System.out.println("===== SIZE SALES =====");
        System.out.printf("%-12s %s%n", "Shoe Size", "Units Sold");
        System.out.println("------------------------------");

        for (int size = 36; size <= 45; size++) {
            int quantity = 0;

            for (Order order : orders) {
                if (order.getStatus() != OrderStatus.COMPLETED) {
                    continue;
                }

                for (OrderItem item : order.getItems()) {
                    if (item.getSize() == size) {
                        quantity += item.getQuantity();
                    }
                }
            }

            // Hide sizes with no sales to keep the report concise.
            if (quantity > 0) {
                System.out.printf("%-12d %d%n", size, quantity);
            }

            totalSold += quantity;

            if (quantity > highestQuantity) {
                highestQuantity = quantity;
                bestSize = size;
            }
        }

        System.out.println("------------------------------");
        System.out.println("Total Units Sold: " + totalSold);
        if (bestSize != 0) {
            System.out.println(
                    "Best Selling Size: " + bestSize
                            + " (" + highestQuantity + " units)"
            );
        } else {
            System.out.println("Best Selling Size: No sales");
        }
    }
}
