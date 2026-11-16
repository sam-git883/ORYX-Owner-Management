
package com.oryx.util;

import com.oryx.model.Order;
import com.oryx.model.OrderItem;
import com.oryx.model.OrderStatus;
import com.oryx.service.Inventory;
import com.oryx.service.BusinessSettings;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.time.LocalDate;

public class FileStorage {

    private static final String ORDER_FILE = "data/orders.csv";
    private static final String INVENTORY_FILE = "data/inventory.csv";
    private static final String RESTOCKED_FILE = "data/restocked_orders.csv";
    private static final String SETTINGS_FILE = "data/business_settings.csv";

    // -----------------------------------------
    // CREATE DATA FOLDER
    // -----------------------------------------

    private void createDataFolder() {
        File folder = new File("data");

        if (!folder.exists()) {
            folder.mkdirs();
        }
    }

    // -----------------------------------------
    // CHECK EXISTING FILES
    // -----------------------------------------

    public boolean hasOrderFile() {
        File file = new File(ORDER_FILE);
        return file.exists();
    }

    public boolean hasInventoryFile() {
        File file = new File(INVENTORY_FILE);
        return file.exists();
    }

    public boolean hasRestockedFile() {
        return new File(RESTOCKED_FILE).exists();
    }

    public boolean hasBusinessSettingsFile() {
        return new File(SETTINGS_FILE).exists();
    }

    // Load current settings. Older installations receive the original defaults.
    public BusinessSettings loadBusinessSettings() {
        File file = new File(SETTINGS_FILE);
        if (!file.exists()) {
            BusinessSettings defaults = new BusinessSettings();
            saveBusinessSettings(defaults);
            return defaults;
        }

        double shoePrice = BusinessSettings.DEFAULT_SHOE_PRICE;
        double profit = BusinessSettings.DEFAULT_PROFIT_PER_SHOE;
        double delivery = BusinessSettings.DEFAULT_DELIVERY_CHARGE;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank() || line.startsWith("Setting,")) continue;
                String[] parts = line.split(",", -1);
                if (parts.length != 2) throw new IllegalArgumentException("Invalid business settings row: " + line);
                double value = Double.parseDouble(parts[1]);
                switch (parts[0].trim()) {
                    case "SHOE_PRICE": shoePrice = value; break;
                    case "PROFIT_PER_SHOE": profit = value; break;
                    case "DELIVERY_CHARGE": delivery = value; break;
                    default: throw new IllegalArgumentException("Unknown business setting: " + parts[0]);
                }
            }
            return new BusinessSettings(shoePrice, profit, delivery);
        } catch (IOException | RuntimeException e) {
            throw new IllegalStateException("Error loading business_settings.csv: " + e.getMessage(), e);
        }
    }

    public void saveBusinessSettings(BusinessSettings settings) {
        createDataFolder();
        try (PrintWriter writer = new PrintWriter(new FileWriter(SETTINGS_FILE))) {
            writer.println("Setting,Value");
            writer.println("SHOE_PRICE," + settings.getShoePrice());
            writer.println("PROFIT_PER_SHOE," + settings.getProfitPerShoe());
            writer.println("DELIVERY_CHARGE," + settings.getDeliveryCharge());
            if (writer.checkError()) throw new IllegalStateException("Could not write business_settings.csv");
        } catch (IOException e) {
            throw new IllegalStateException("Error saving business_settings.csv: " + e.getMessage(), e);
        }
    }

    // -----------------------------------------
    // SAVE ORDERS
    // -----------------------------------------

    public void saveOrders(List<Order> orders) {

        createDataFolder();

        try (PrintWriter writer = new PrintWriter(
                new FileWriter(ORDER_FILE))) {

            writer.println(
                    "OrderID,CustomerName,Address,District,PhoneNumber,Status,Color,Size,Quantity,UnitPrice,GoodReturned,DamagedReturned,OrderDate,CompletedDate,ReturnedDate,ProfitPerShoe,DeliveryCharge"
            );

            for (Order order : orders) {

                for (OrderItem item : order.getItems()) {

                    writer.println(
                            order.getOrderId() + ","
                                    + cleanCSV(order.getCustomerName()) + ","
                                    + cleanCSV(order.getAddress()) + ","
                                    + cleanCSV(order.getDistrict()) + ","
                                    + cleanCSV(order.getPhoneNumber()) + ","
                                    + order.getStatus() + ","
                                    + item.getColor() + ","
                                    + item.getSize() + ","
                                    + item.getQuantity() + ","
                                    + item.getUnitPrice() + ","
                                    + item.getGoodConditionQuantity() + ","
                                    + item.getDamagedQuantity() + ","
                                    + dateToString(order.getOrderDate()) + ","
                                    + dateToString(order.getCompletedDate()) + ","
                                    + dateToString(order.getReturnedDate()) + ","
                                    + order.getProfitPerShoe() + ","
                                    + order.getDeliveryCharge()
                    );
                }
            }

            System.out.println("Orders saved successfully!");

            if (writer.checkError()) {
                throw new IllegalStateException("Could not write orders.csv");
            }

        } catch (IOException e) {
            throw new IllegalStateException("Error saving orders.csv: " + e.getMessage(), e);
        }
    }

    private String dateToString(LocalDate date) {
        return date == null ? "" : date.toString();
    }

    private LocalDate parseDate(String value) {
        return value == null || value.isBlank()
                ? null : LocalDate.parse(value);
    }

    // Remove commas from text to protect the simple CSV format
    private String cleanCSV(String value) {
        return value.replace(",", " ");
    }

    // -----------------------------------------
    // LOAD ORDERS
    // -----------------------------------------

    public List<Order> loadOrders() {

        List<Order> orders = new ArrayList<>();

        File file = new File(ORDER_FILE);

        if (!file.exists()) {

            System.out.println("Order file not found.");

            return orders;
        }

        try (BufferedReader reader = new BufferedReader(
                new FileReader(file))) {

            // Skip CSV header
            reader.readLine();

            String line;

            while ((line = reader.readLine()) != null) {

                if (line.isBlank()) {
                    continue;
                }

                String[] data = line.split(",", -1);

                if (data.length != 17 && data.length != 15 && data.length != 12
                        && data.length != 10 && data.length != 8) {

                    throw new IllegalArgumentException(
                            "Invalid CSV data: " + line
                    );
                }

                int orderId = Integer.parseInt(data[0]);

                String customerName;
                String address;
                String district;
                String phoneNumber;
                OrderStatus savedStatus;
                String color;
                int size;
                int quantity;
                double unitPrice;
                int goodReturned = 0;
                int damagedReturned = 0;

                // Current 17-column format stores return details, dates, and financial settings.
                // Older 15/12/10/8-column files remain supported.
                if (data.length == 17 || data.length == 15 || data.length == 12 || data.length == 10) {

                    customerName = data[1];
                    address = data[2];
                    district = data[3];
                    phoneNumber = data[4];

                    savedStatus = OrderStatus.valueOf(data[5]);

                    color = data[6];
                    size = Integer.parseInt(data[7]);
                    quantity = Integer.parseInt(data[8]);
                    unitPrice = Double.parseDouble(data[9]);

                    if (data.length >= 12) {
                        goodReturned = Integer.parseInt(data[10]);
                        damagedReturned = Integer.parseInt(data[11]);
                    }

                } else {

                    // Old 8-column format
                    customerName = data[1];
                    address = "Not provided";
                    district = data[2];
                    phoneNumber = "Not provided";

                    savedStatus = OrderStatus.valueOf(data[3]);

                    color = data[4];
                    size = Integer.parseInt(data[5]);
                    quantity = Integer.parseInt(data[6]);
                    unitPrice = Double.parseDouble(data[7]);
                }

                Order order = null;

                // Find existing order
                for (Order existingOrder : orders) {

                    if (existingOrder.getOrderId() == orderId) {

                        order = existingOrder;
                        break;
                    }
                }

                // Create order if it does not exist
                if (order == null) {

                    if (phoneNumber.matches("0\\d{9}")) {

                        if (data.length == 17) {
                            order = new Order(
                                    orderId,
                                    customerName,
                                    address,
                                    district,
                                    phoneNumber,
                                    Double.parseDouble(data[15]),
                                    Double.parseDouble(data[16])
                            );
                        } else {
                            // Legacy files use the original default financial values.
                            order = new Order(
                                    orderId, customerName, address, district, phoneNumber
                            );
                        }

                    } else {

                        // Older demo records may contain placeholder contact details.
                        order = new Order(orderId, customerName, district);
                    }

                    // Restore saved status
                    if (savedStatus != OrderStatus.PENDING) {

                        order.updateStatus(OrderStatus.DISPATCHED);

                        if (savedStatus == OrderStatus.COMPLETED
                                || savedStatus == OrderStatus.RETURNED) {

                            order.updateStatus(savedStatus);
                        }
                    }

                    if (data.length == 17 || data.length == 15) {
                        order.restoreDates(
                                parseDate(data[12]),
                                parseDate(data[13]),
                                parseDate(data[14])
                        );
                    } else {
                        // Existing records have no reliable historical dates.
                        order.restoreDates(null, null, null);
                    }

                    // Legacy CSV rows used the original business values.
                    if (data.length == 17) {
                        order.restoreFinancialSettings(
                                Double.parseDouble(data[15]),
                                Double.parseDouble(data[16])
                        );
                    }

                    orders.add(order);
                }

                // Add order item
                OrderItem loadedItem = new OrderItem(
                        color,
                        size,
                        quantity,
                        unitPrice
                );

                loadedItem.restoreReturnDetails(goodReturned, damagedReturned);
                order.addItem(loadedItem);
            }

            System.out.println("Orders loaded successfully!");

        } catch (IOException | RuntimeException e) {
            throw new IllegalStateException("Error loading orders.csv: " + e.getMessage(), e);
        }

        return orders;
    }

    // -----------------------------------------
    // SAVE INVENTORY
    // -----------------------------------------

    public void saveInventory(Inventory inventory) {

        createDataFolder();

        try (PrintWriter writer = new PrintWriter(
                new FileWriter(INVENTORY_FILE))) {

            writer.println("Variant,Quantity");

            Map<String, Integer> stock =
                    inventory.getAllStock();

            for (Map.Entry<String, Integer> entry : stock.entrySet()) {

                writer.println(
                        entry.getKey() + "," + entry.getValue()
                );
            }

            System.out.println("Inventory saved successfully!");

            if (writer.checkError()) {
                throw new IllegalStateException("Could not write inventory.csv");
            }

        } catch (IOException e) {
            throw new IllegalStateException("Error saving inventory.csv: " + e.getMessage(), e);
        }
    }

    // -----------------------------------------
    // LOAD INVENTORY
    // -----------------------------------------

    public void loadInventory(Inventory inventory) {

        File file = new File(INVENTORY_FILE);

        if (!file.exists()) {

            System.out.println("Inventory file not found.");

            return;
        }

        try (BufferedReader reader = new BufferedReader(
                new FileReader(file))) {

            // Skip CSV header
            reader.readLine();

            String line;

            while ((line = reader.readLine()) != null) {

                if (line.isBlank()) {
                    continue;
                }

                String[] data = line.split(",", -1);

                if (data.length != 2) {

                    throw new IllegalArgumentException(
                            "Invalid inventory data: " + line
                    );
                }

                String[] variant = data[0].split("-", -1);

                if (variant.length != 2) {

                    throw new IllegalArgumentException(
                            "Invalid stock variant: " + data[0]
                    );
                }

                String color = variant[0];

                int size = Integer.parseInt(variant[1]);

                int quantity = Integer.parseInt(data[1]);

                inventory.addStock(color, size, quantity);
            }

            System.out.println("Inventory loaded successfully!");

        } catch (IOException | RuntimeException e) {
            throw new IllegalStateException("Error loading inventory.csv: " + e.getMessage(), e);
        }
    }

    // -----------------------------------------
    // SAVE RESTOCKED ORDER IDS
    // -----------------------------------------

    public void saveRestockedOrderIds(List<Integer> orderIds) {

        createDataFolder();

        try (PrintWriter writer = new PrintWriter(
                new FileWriter(RESTOCKED_FILE))) {

            writer.println("OrderID");

            for (int orderId : orderIds) {

                writer.println(orderId);
            }

            System.out.println(
                    "Restocked orders saved successfully!"
            );

            if (writer.checkError()) {
                throw new IllegalStateException("Could not write restocked_orders.csv");
            }

        } catch (IOException e) {
            throw new IllegalStateException("Error saving restocked_orders.csv: " + e.getMessage(), e);
        }
    }

    // -----------------------------------------
    // LOAD RESTOCKED ORDER IDS
    // -----------------------------------------

    public List<Integer> loadRestockedOrderIds() {

        List<Integer> orderIds = new ArrayList<>();

        File file = new File(RESTOCKED_FILE);

        if (!file.exists()) {

            System.out.println(
                    "Restocked orders file not found."
            );

            return orderIds;
        }

        try (BufferedReader reader = new BufferedReader(
                new FileReader(file))) {

            // Skip CSV header
            reader.readLine();

            String line;

            while ((line = reader.readLine()) != null) {

                if (!line.isBlank()) {

                    orderIds.add(
                            Integer.parseInt(line.trim())
                    );
                }
            }

            System.out.println(
                    "Restocked orders loaded successfully!"
            );

        } catch (IOException | RuntimeException e) {
            throw new IllegalStateException("Error loading restocked_orders.csv: " + e.getMessage(), e);
        }

        return orderIds;
    }
}
