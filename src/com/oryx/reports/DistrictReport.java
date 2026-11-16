package com.oryx.reports;

import com.oryx.model.Order;
import com.oryx.model.OrderItem;
import com.oryx.model.OrderStatus;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DistrictReport extends Report {

    @Override
    public void generateReport(List<Order> orders) {

        Map<String, Integer> salesByDistrict = new HashMap<>();

        for (Order order : orders) {
            if (order.getStatus() != OrderStatus.COMPLETED) {
                continue;
            }

            int quantity = 0;
            for (OrderItem item : order.getItems()) {
                quantity += item.getQuantity();
            }

            String district = order.getDistrict().trim();
            salesByDistrict.put(
                    district,
                    salesByDistrict.getOrDefault(district, 0) + quantity
            );
        }

        List<Map.Entry<String, Integer>> ranking =
                new ArrayList<>(salesByDistrict.entrySet());

        ranking.sort(
                Comparator.<Map.Entry<String, Integer>>comparingInt(
                        Map.Entry::getValue
                ).reversed().thenComparing(Map.Entry::getKey)
        );

        System.out.println("===== DISTRICT SALES =====");

        if (ranking.isEmpty()) {
            System.out.println("No completed sales in this period.");
            return;
        }

        System.out.printf("%-18s %s%n", "District", "Units Sold");
        System.out.println("------------------------------");

        for (Map.Entry<String, Integer> entry : ranking) {
            if (entry.getValue() > 0) {
                System.out.printf(
                        "%-18s %d%n", entry.getKey(), entry.getValue()
                );
            }
        }

        Map.Entry<String, Integer> topDistrict = ranking.get(0);
        System.out.println("------------------------------");
        System.out.println(
                "Top District: " + topDistrict.getKey()
                        + " (" + topDistrict.getValue() + " units)"
        );
    }
}
