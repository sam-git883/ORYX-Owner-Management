
package com.oryx.customer;

import com.oryx.model.Product;
import com.oryx.model.Order;
import com.oryx.model.OrderItem;

import com.oryx.service.Inventory;
import com.oryx.util.FileStorage;
import com.oryx.service.BusinessSettings;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CustomerMain {

    private static final Scanner scanner = new Scanner(System.in);

    private static BusinessSettings businessSettings;
    private static Product product;

    public static void main(String[] args) {

        Inventory inventory = new Inventory();
        FileStorage fileStorage = new FileStorage();
        businessSettings = fileStorage.loadBusinessSettings();
        product = new Product(1, "ORYX CLASSIC", businessSettings.getShoePrice());

        // Load existing inventory
        File inventoryFile = new File("data/inventory.csv");

        if (inventoryFile.exists()) {

            System.out.println("Loading product availability...");

            fileStorage.loadInventory(inventory);

        } else {

            System.out.println("Stock information is not available.");
        }

        // Load existing orders
        List<Order> orders = fileStorage.loadOrders();

        // Start customer menu
        runCustomerMenu(inventory, orders, fileStorage);
    }

    // -----------------------------------------
    // CUSTOMER MAIN MENU
    // -----------------------------------------

    public static void displayMenu() {

        System.out.println("\n========================================");
        System.out.println("              WELCOME TO ORYX");
        System.out.println("           YOUR STYLE, YOUR CHOICE");
        System.out.println("========================================");

        System.out.println("01. View Product");
        System.out.println("02. Check Stock");
        System.out.println("03. Place Order");
        System.out.println("04. Exit");

        System.out.println("========================================");
        System.out.print("Enter Your Choice: ");
    }

    // -----------------------------------------
    // RUN CUSTOMER MENU
    // -----------------------------------------

    public static void runCustomerMenu(
            Inventory inventory,
            List<Order> orders,
            FileStorage fileStorage) {

        boolean running = true;

        while (running) {

            displayMenu();

            int choice = readInt();

            switch (choice) {

                case 1:
                    viewProduct();
                    break;

                case 2:
                    checkStock(inventory);
                    break;

                case 3:
                    placeOrder(inventory, orders, fileStorage);
                    break;

                case 4:
                    System.out.println("\nThank you for choosing ORYX!");
                    System.out.println("Have a nice day!");

                    running = false;
                    break;

                default:
                    System.out.println("\nInvalid menu choice!");
            }
        }
    }

    // -----------------------------------------
    // 01. VIEW PRODUCT
    // -----------------------------------------

    public static void viewProduct() {

        System.out.println("\n========== PRODUCT DETAILS ==========");

        System.out.println("Product ID   : " + product.getProductId());
        System.out.println("Product Name : " + product.getProductName());
        System.out.println("Price        : Rs. " + product.getPrice());

        System.out.println("\nAvailable Colors:");

        System.out.println("01. BLACK");
        System.out.println("02. WHITE");
        System.out.println("03. BLUE");
        System.out.println("04. BROWN");

        System.out.println("\nAvailable Sizes:");

        for (int size = 36; size <= 45; size++) {
            System.out.print(size + " ");
        }

        System.out.println("\n\nDelivery Charge : Rs. " + businessSettings.getDeliveryCharge());
        System.out.println("Delivery Time   : 3-5 Working Days");
        System.out.println("Payment Method  : Cash on Delivery");

        System.out.println("=====================================");
    }

    // -----------------------------------------
    // 02. CHECK STOCK
    // -----------------------------------------

    public static void checkStock(Inventory inventory) {

        boolean checking = true;

        String[] colors = {
                "BLACK", "WHITE", "BLUE", "BROWN"
        };

        while (checking) {

            System.out.println("\n========== CHECK STOCK ==========");

            for (int i = 0; i < colors.length; i++) {
                System.out.println((i + 1) + ". " + colors[i]);
            }

            System.out.println("5. Back to Main Menu");

            System.out.print("\nSelect Color: ");

            int colorChoice = readInt();

            if (colorChoice == 5) {
                checking = false;
                continue;
            }

            if (colorChoice < 1 || colorChoice > 4) {
                System.out.println("Invalid color selection!");
                continue;
            }

            String selectedColor = colors[colorChoice - 1];

            boolean hasStock = false;

            System.out.println(
                    "\n========== " + selectedColor + " STOCK =========="
            );

            System.out.println("\nAvailable Sizes:\n");

            int optionNumber = 1;

            for (int size = 36; size <= 45; size++) {

                int quantity = inventory.getStock(selectedColor, size);

                if (quantity > 0) {

                    hasStock = true;

                    System.out.println(
                            optionNumber + ". Size " + size
                    );

                    optionNumber++;
                }
            }

            if (!hasStock) {

                System.out.println(
                        "Sorry! No stock available for " + selectedColor
                );

                continue;
            }

            System.out.println(optionNumber + ". Back");

            System.out.print("\nSelect Size: ");

            int sizeChoice = readInt();

            if (sizeChoice == optionNumber) {
                continue;
            }

            if (sizeChoice < 1 || sizeChoice >= optionNumber) {
                System.out.println("Invalid size selection!");
                continue;
            }

            int selectedSize = 0;
            int currentOption = 0;

            for (int size = 36; size <= 45; size++) {

                if (inventory.getStock(selectedColor, size) > 0) {

                    currentOption++;

                    if (currentOption == sizeChoice) {
                        selectedSize = size;
                        break;
                    }
                }
            }

            int availableQuantity = inventory.getStock(
                    selectedColor,
                    selectedSize
            );

            System.out.println("\n========== STOCK DETAILS ==========");

            System.out.println("Color: " + selectedColor);
            System.out.println("Size : " + selectedSize);

            System.out.println(
                    "Available Quantity: " + availableQuantity + " Pairs"
            );

            System.out.println("Status: IN STOCK");

            System.out.println("===================================");
        }
    }

    // -----------------------------------------
    // 03. PLACE ORDER
    // -----------------------------------------

    public static void placeOrder(
            Inventory inventory,
            List<Order> orders,
            FileStorage fileStorage) {

        System.out.println("\n========== PLACE ORDER ==========");

        if (inventory.getAllStock().isEmpty()) {
            System.out.println("Sorry! Products are currently unavailable.");
            return;
        }

        // Generate next Order ID
        int orderId = 201;

        for (Order order : orders) {

            if (order.getOrderId() >= orderId) {
                orderId = order.getOrderId() + 1;
            }
        }

        try {

            // Collect customer details
            Order newOrder = collectCustomerDetails(orderId);

            // Add shoes to order
            boolean addingItems = true;

            while (addingItems) {

                String[] colors = {
                        "BLACK", "WHITE", "BLUE", "BROWN"
                };

                System.out.println("\nAvailable Colors:");

                for (int i = 0; i < colors.length; i++) {
                    System.out.println((i + 1) + ". " + colors[i]);
                }

                System.out.print("Select Color: ");

                int colorChoice = readInt();

                if (colorChoice < 1 || colorChoice > 4) {
                    System.out.println("Invalid color selection!");
                    continue;
                }

                String selectedColor = colors[colorChoice - 1];

                // Display available sizes
                List<Integer> availableSizes = new ArrayList<>();

                System.out.println("\nAvailable Sizes:");

                for (int size = 36; size <= 45; size++) {

                    if (inventory.getStock(selectedColor, size) > 0) {

                        availableSizes.add(size);

                        System.out.println(
                                availableSizes.size() + ". Size " + size
                        );
                    }
                }

                if (availableSizes.isEmpty()) {
                    System.out.println("No stock available for this color!");
                    continue;
                }

                System.out.print("Select Size: ");

                int sizeChoice = readInt();

                if (sizeChoice < 1 || sizeChoice > availableSizes.size()) {
                    System.out.println("Invalid size selection!");
                    continue;
                }

                int selectedSize = availableSizes.get(sizeChoice - 1);

                // Get quantity
                int availableStock = inventory.getStock(
                        selectedColor,
                        selectedSize
                );

                System.out.println("Available Stock: " + availableStock);

                System.out.print("Enter Quantity: ");

                int quantity = readInt();

                if (quantity <= 0 || quantity > availableStock) {
                    System.out.println("Invalid quantity or insufficient stock!");
                    continue;
                }

                // Add item
                OrderItem item = new OrderItem(
                        selectedColor,
                        selectedSize,
                        quantity,
                        businessSettings.getShoePrice()
                );

                newOrder.addItem(item);

                System.out.println("Item added to order successfully!");

                // Ask whether customer wants more items
                System.out.print("\nAdd another shoe variant? (Y/N): ");

                String answer = scanner.nextLine().trim();

                if (answer.equalsIgnoreCase("N")) {
                    addingItems = false;
                } else if (!answer.equalsIgnoreCase("Y")) {
                    System.out.println("Invalid choice. Please enter Y or N.");
                }
            }

            // Display order summary
            displayOrderSummary(newOrder);

            // Confirm order
            System.out.print("\nConfirm Order? (Y/N): ");

            String confirmation = scanner.nextLine().trim();

            if (confirmation.equalsIgnoreCase("Y")) {

                orders.add(newOrder);

                fileStorage.saveOrders(orders);

                System.out.println("\nOrder placed successfully!");
                System.out.println("Your Order ID: " + orderId);
                System.out.println("Order Status: PENDING");
                System.out.println("Payment Method: Cash on Delivery");
                System.out.println("Delivery Time: 3-5 Working Days");

                System.out.println("\nPlease keep your Order ID.");

            } else {

                System.out.println("\nOrder was not confirmed.");
            }

        } catch (IllegalArgumentException e) {

            System.out.println("\nError: " + e.getMessage());
        }
    }

    // -----------------------------------------
    // COLLECT CUSTOMER DETAILS
    // -----------------------------------------

    public static Order collectCustomerDetails(int orderId) {

        System.out.println("\n========== CUSTOMER DETAILS ==========");

        String name;
        String address;
        String district;
        String phoneNumber;

        // Customer Name
        while (true) {

            System.out.print("Enter Customer Name: ");

            name = scanner.nextLine().trim();

            if (!name.isEmpty()) {
                break;
            }

            System.out.println("Customer name cannot be empty!");
        }

        // Address
        while (true) {

            System.out.print("Enter Delivery Address: ");

            address = scanner.nextLine().trim();

            if (!address.isEmpty()) {
                break;
            }

            System.out.println("Address cannot be empty!");
        }

        // District
        while (true) {

            System.out.print("Enter District: ");

            district = scanner.nextLine().trim();

            if (!district.isEmpty()) {
                break;
            }

            System.out.println("District cannot be empty!");
        }

        // Phone Number
        while (true) {

            System.out.print("Enter Phone Number: ");

            phoneNumber = scanner.nextLine().trim();

            if (phoneNumber.matches("0\\d{9}")) {
                break;
            }

            System.out.println(
                    "Invalid phone number! Enter 10 digits starting with 0."
            );
        }

        return new Order(
                orderId,
                name,
                address,
                district,
                phoneNumber,
                businessSettings.getProfitPerShoe(),
                businessSettings.getDeliveryCharge()
        );
    }

    // -----------------------------------------
    // DISPLAY ORDER SUMMARY
    // -----------------------------------------

    public static void displayOrderSummary(Order order) {

        System.out.println("\n========================================");
        System.out.println("              ORDER SUMMARY");
        System.out.println("========================================");

        System.out.println("Order ID      : " + order.getOrderId());
        System.out.println("Customer Name : " + order.getCustomerName());
        System.out.println("Address       : " + order.getAddress());
        System.out.println("District      : " + order.getDistrict());
        System.out.println("Phone Number  : " + order.getPhoneNumber());

        System.out.println("\nOrder Items:");

        for (OrderItem item : order.getItems()) {

            System.out.println(
                    item.getColor()
                            + " | Size: " + item.getSize()
                            + " | Quantity: " + item.getQuantity()
                            + " | Subtotal: Rs. " + item.getSubtotal()
            );
        }

        double shoeTotal = order.getTotalAmount();

        double finalTotal = shoeTotal + order.getDeliveryCharge();

        System.out.println("\n----------------------------------------");

        System.out.println("Shoe Total     : Rs. " + shoeTotal);
        System.out.println("Delivery Charge: Rs. " + order.getDeliveryCharge());

        System.out.println("----------------------------------------");

        System.out.println("TOTAL AMOUNT   : Rs. " + finalTotal);

        System.out.println("\nPayment Method : Cash on Delivery");
        System.out.println("Order Status   : PENDING");

        System.out.println("========================================");
    }

    // -----------------------------------------
    // READ INTEGER INPUT
    // -----------------------------------------

    public static int readInt() {

        while (true) {

            String input = scanner.nextLine().trim();

            try {

                return Integer.parseInt(input);

            } catch (NumberFormatException e) {

                System.out.print("Please enter a valid number: ");
            }
        }
    }
}
