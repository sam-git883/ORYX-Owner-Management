package com.oryx;

import com.oryx.model.Order;
import com.oryx.model.OrderItem;
import com.oryx.model.OrderStatus;

import com.oryx.service.Inventory;
import com.oryx.service.OrderService;
import com.oryx.service.FinancialService;
import com.oryx.service.ReturnService;
import com.oryx.service.BusinessSettings;

import com.oryx.reports.Report;
import com.oryx.reports.DistrictReport;
import com.oryx.reports.ColorReport;
import com.oryx.reports.SizeReport;

import com.oryx.util.FileStorage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.Year;

public class Main {

    private static Scanner scanner = new Scanner(System.in);

    // Allows login and the main menu to share one input scanner.
    public static void setScanner(Scanner sharedScanner) {
        scanner = sharedScanner;
    }

    public static void main(String[] args) {

        FileStorage fileStorage = new FileStorage();
        BusinessSettings businessSettings;
        try {
            businessSettings = fileStorage.loadBusinessSettings();
        } catch (RuntimeException e) {
            System.out.println("Business settings could not be loaded safely: " + e.getMessage());
            return;
        }
        Inventory inventory = new Inventory();

        List<Order> orders = new ArrayList<>();

        OrderService orderService = new OrderService(inventory);
        FinancialService financialService = new FinancialService();
        ReturnService returnService = new ReturnService(inventory);

        // Create data folder
        File dataFolder = new File("data");

        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        System.out.println("==================================");
        System.out.println("   ORYX OWNER MANAGEMENT SYSTEM");
        System.out.println("==================================");

        // Do not recreate initial data if only one required file is missing.
        boolean orderFileExists = fileStorage.hasOrderFile();
        boolean inventoryFileExists = fileStorage.hasInventoryFile();
        boolean restockedFileExists = fileStorage.hasRestockedFile();

        if (orderFileExists && inventoryFileExists) {

            System.out.println("\nLoading existing system data...");

            try {
                fileStorage.loadInventory(inventory);
                orders = fileStorage.loadOrders();

                returnService.loadRestockedOrderIds(
                        fileStorage.loadRestockedOrderIds(), orders
                );

                System.out.println("Existing data loaded successfully!");

            } catch (RuntimeException e) {
                System.out.println("\nSYSTEM STARTUP STOPPED.");
                System.out.println("Saved data could not be loaded safely.");
                System.out.println("Please check the CSV files in the data folder.");
                System.out.println("Details: " + e.getMessage());
                return;
            }

        } else if (!orderFileExists && !inventoryFileExists
                && !restockedFileExists) {

            System.out.println("\nFirst system startup...");
            System.out.println("Creating initial inventory...");

            createInitialStock(inventory);
            orders = new ArrayList<>();

            try {
                saveData(orders, inventory, fileStorage, returnService);
            } catch (RuntimeException e) {
                System.out.println("Initial data could not be saved: " + e.getMessage());
                return;
            }

            System.out.println("Initial inventory created successfully!");
            System.out.println("New orders can be placed through the Customer System.");

        } else {
            System.out.println("\nSYSTEM STARTUP STOPPED.");
            System.out.println("One required CSV file is missing.");
            System.out.println("The system will not recreate data because this could overwrite your records.");
            System.out.println("Required files: data/orders.csv and data/inventory.csv");
            System.out.println("Existing restocked_orders.csv was also preserved.");
            return;
        }

        // Start console menu
        runMenu(
                orders,
                inventory,
                orderService,
                financialService,
                returnService,
                fileStorage,
                businessSettings
        );
    }

    // -----------------------------------------
    // CREATE INITIAL STOCK
    // -----------------------------------------

    public static void createInitialStock(Inventory inventory) {

        inventory.addStock("BLACK", 42, 20);
        inventory.addStock("BLUE", 40, 20);
        inventory.addStock("WHITE", 39, 20);
        inventory.addStock("BROWN", 43, 20);
    }

    // -----------------------------------------
    // MAIN MENU
    // -----------------------------------------

    public static void displayMenu() {

        System.out.println("\n==================================");
        System.out.println("   ORYX OWNER MANAGEMENT SYSTEM");
        System.out.println("==================================");

        System.out.println("01. View All Orders");
        System.out.println("02. Search Order");
        System.out.println("03. Update Order Status");
        System.out.println("04. Manage Inventory");
        System.out.println("05. Manage Returns");
        System.out.println("06. Financial Report");
        System.out.println("07. Sales Reports");
        System.out.println("08. Save Data");
        System.out.println("09. Manage Business Settings");
        System.out.println("10. Exit System");

        System.out.println("==================================");
        System.out.print("Enter your choice: ");
    }

    // -----------------------------------------
    // RUN MENU
    // -----------------------------------------

    public static void runMenu(
            List<Order> orders,
            Inventory inventory,
            OrderService orderService,
            FinancialService financialService,
            ReturnService returnService,
            FileStorage fileStorage,
            BusinessSettings businessSettings) {

        boolean running = true;

        while (running) {

            displayMenu();

            int choice = readInt();

            switch (choice) {

                case 1:
                    viewOrdersMenu(orders);
                    break;

                case 2:
                    searchOrder(orders);
                    break;

                case 3:
                    updateOrderStatus(orders, orderService);
                    break;

                case 4:
                    manageInventory(inventory);
                    break;

                case 5:
                    manageReturns(orders, returnService, inventory, fileStorage);
                    break;

                case 6:
                    financialReportMenu(orders, financialService);
                    break;

                case 7:
                    salesReportMenu(orders);
                    break;

                case 8:
                    saveData(
                            orders,
                            inventory,
                            fileStorage,
                            returnService
                    );
                    break;

                case 9:
                    manageBusinessSettings(businessSettings, fileStorage);
                    break;

                case 10:

                    saveData(
                            orders,
                            inventory,
                            fileStorage,
                            returnService
                    );

                    System.out.println("\nThank you for using ORYX!");
                    System.out.println("System closed successfully.");

                    running = false;
                    break;

                default:
                    System.out.println("\nInvalid menu choice!");
            }
        }
    }

    // -----------------------------------------
    // 01. VIEW ORDERS SUB MENU
    // -----------------------------------------

    public static void viewOrdersMenu(List<Order> orders) {

        boolean viewing = true;

        while (viewing) {

            System.out.println("\n========================================");
            System.out.println("                 VIEW ORDERS");
            System.out.println("========================================");
            System.out.println("01. All Orders");
            System.out.println("02. Pending Orders");
            System.out.println("03. Dispatched Orders");
            System.out.println("04. Completed Orders");
            System.out.println("05. Returned Orders");
            System.out.println("06. Back to Main Menu");
            System.out.println("========================================");
            System.out.print("Enter your choice: ");

            int choice = readInt();

            switch (choice) {

                case 1:
                    viewAllOrders(orders);
                    break;

                case 2:
                    viewOrdersByStatus(orders, OrderStatus.PENDING);
                    break;

                case 3:
                    viewOrdersByStatus(orders, OrderStatus.DISPATCHED);
                    break;

                case 4:
                    viewOrdersByStatus(orders, OrderStatus.COMPLETED);
                    break;

                case 5:
                    viewOrdersByStatus(orders, OrderStatus.RETURNED);
                    break;

                case 6:
                    viewing = false;
                    break;

                default:
                    System.out.println("\nInvalid menu choice!");
            }
        }
    }

    // -----------------------------------------
    // DISPLAY ORDERS BY STATUS
    // -----------------------------------------

    public static void viewOrdersByStatus(List<Order> orders, OrderStatus status) {

        System.out.println("\n========== " + status + " ORDERS ==========");

        int count = 0;

        for (Order order : orders) {

            if (order.getStatus() == status) {
                displayOrderDetails(order);
                count++;
            }
        }

        if (count == 0) {
            System.out.println("No " + status.toString().toLowerCase() + " orders available.");
        }

        System.out.println("\nTotal " + status + " Orders: " + count);
    }

    // -----------------------------------------
    // DISPLAY ONE ORDER
    // -----------------------------------------

    public static void displayOrderDetails(Order order) {

        System.out.println("\nOrder ID      : " + order.getOrderId());
        System.out.println("Customer Name : " + order.getCustomerName());
        System.out.println("District      : " + order.getDistrict());
        System.out.println("Status        : " + order.getStatus());

        System.out.println("Items:");

        for (OrderItem item : order.getItems()) {

            System.out.println(
                    item.getColor()
                            + " | Size: " + item.getSize()
                            + " | Quantity: " + item.getQuantity()
                            + " | Subtotal: Rs. " + item.getSubtotal()
            );
        }

        System.out.println("Total Quantity: " + order.getTotalQuantity());
        System.out.println("Total Amount  : Rs. " + order.getTotalAmount());
        System.out.println("----------------------------------");
    }

    // -----------------------------------------
    // 01. VIEW ALL ORDERS
    // -----------------------------------------

    public static void viewAllOrders(List<Order> orders) {

        System.out.println("\n========== ALL ORDERS ==========");

        if (orders.isEmpty()) {
            System.out.println("No orders available.");
            System.out.println("Total Orders: 0");
            return;
        }

        for (Order order : orders) {
            displayOrderDetails(order);
        }

        System.out.println("Total Orders: " + orders.size());
    }

    // -----------------------------------------
    // 02. SEARCH ORDER
    // -----------------------------------------

    public static void searchOrder(List<Order> orders) {

        System.out.println("\n========== SEARCH ORDER ==========");

        System.out.print("Enter Order ID: ");
        int orderId = readInt();

        Order order = findOrderById(orders, orderId);

        if (order == null) {
            System.out.println("Order not found!");
            return;
        }

        System.out.println("\nOrder ID      : " + order.getOrderId());
        System.out.println("Customer Name : " + order.getCustomerName());
        System.out.println("District      : " + order.getDistrict());
        System.out.println("Status        : " + order.getStatus());

        System.out.println("\nOrder Items:");

        for (OrderItem item : order.getItems()) {

            System.out.println(
                    item.getColor()
                            + " | Size: " + item.getSize()
                            + " | Quantity: " + item.getQuantity()
            );
        }

        System.out.println("Total Quantity: " + order.getTotalQuantity());
        System.out.println("Total Amount  : Rs. " + order.getTotalAmount());
    }

    // -----------------------------------------
    // FIND ORDER BY ID
    // -----------------------------------------

    public static Order findOrderById(List<Order> orders, int orderId) {

        for (Order order : orders) {

            if (order.getOrderId() == orderId) {
                return order;
            }
        }

        return null;
    }

    // -----------------------------------------
    // 03. UPDATE ORDER STATUS
    // -----------------------------------------

    public static void updateOrderStatus(
            List<Order> orders,
            OrderService orderService) {

        System.out.println("\n========== UPDATE ORDER STATUS ==========");

        System.out.print("Enter Order ID: ");
        int orderId = readInt();

        Order order = findOrderById(orders, orderId);

        if (order == null) {
            System.out.println("Order not found!");
            return;
        }

        System.out.println("Current Status: " + order.getStatus());

        try {

            if (order.getStatus() == OrderStatus.PENDING) {

                System.out.println("1. Mark as DISPATCHED");
                System.out.print("Enter choice: ");

                int choice = readInt();

                if (choice == 1) {

                    orderService.processOrder(order);

                } else {

                    System.out.println("Invalid choice!");
                }

            } else if (order.getStatus() == OrderStatus.DISPATCHED) {

                System.out.println("1. Mark as COMPLETED");
                System.out.println("2. Mark as RETURNED");

                System.out.print("Enter choice: ");

                int choice = readInt();

                if (choice == 1) {

                    order.updateStatus(OrderStatus.COMPLETED);

                    System.out.println("Order marked as COMPLETED!");

                } else if (choice == 2) {

                    order.updateStatus(OrderStatus.RETURNED);

                    System.out.println("Order marked as RETURNED!");
                    System.out.println("Return loss: Rs. " + order.getDeliveryCharge());

                } else {

                    System.out.println("Invalid choice!");
                }

            } else {

                System.out.println("This order cannot be updated.");
            }

        } catch (IllegalArgumentException e) {

            System.out.println("Error: " + e.getMessage());
        }
    }

    // -----------------------------------------
    // 09. MANAGE BUSINESS SETTINGS
    // -----------------------------------------

    public static void manageBusinessSettings(
            BusinessSettings settings, FileStorage fileStorage) {

        boolean managing = true;
        while (managing) {
            System.out.println("\n========== BUSINESS SETTINGS ==========");
            System.out.println("Current Shoe Price       : Rs. " + settings.getShoePrice());
            System.out.println("Current Profit per Shoe  : Rs. " + settings.getProfitPerShoe());
            System.out.println("Current Delivery Charge  : Rs. " + settings.getDeliveryCharge());
            System.out.println("\n1. Change Shoe Price");
            System.out.println("2. Change Profit per Shoe");
            System.out.println("3. Change Delivery Charge");
            System.out.println("4. Back to Main Menu");
            System.out.print("Enter choice: ");

            int choice = readInt();
            if (choice == 4) {
                managing = false;
                continue;
            }
            if (choice < 1 || choice > 3) {
                System.out.println("Invalid menu choice!");
                continue;
            }

            System.out.print("Enter new amount (Rs.): ");
            double newValue = readDouble();
            double oldValue;
            try {
                if (choice == 1) oldValue = settings.getShoePrice();
                else if (choice == 2) oldValue = settings.getProfitPerShoe();
                else oldValue = settings.getDeliveryCharge();

                switch (choice) {
                    case 1: settings.setShoePrice(newValue); break;
                    case 2: settings.setProfitPerShoe(newValue); break;
                    case 3: settings.setDeliveryCharge(newValue); break;
                }
                try {
                    fileStorage.saveBusinessSettings(settings);
                    System.out.println("Business setting updated and saved successfully.");
                    System.out.println("This change will apply to new orders only.");
                } catch (RuntimeException saveError) {
                    // Restore the in-memory value too, so an unsaved change is not used.
                    if (choice == 1) settings.setShoePrice(oldValue);
                    else if (choice == 2) settings.setProfitPerShoe(oldValue);
                    else settings.setDeliveryCharge(oldValue);
                    System.out.println("Settings could not be saved. Previous value restored.");
                    System.out.println("Details: " + saveError.getMessage());
                }
            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    // -----------------------------------------
    // 04. MANAGE INVENTORY
    // -----------------------------------------

    public static void manageInventory(Inventory inventory) {

        boolean managing = true;

        while (managing) {

            System.out.println("\n========== INVENTORY MANAGEMENT ==========");

            System.out.println("1. View Current Stock");
            System.out.println("2. Add Stock");
            System.out.println("3. Back to Main Menu");

            System.out.print("Enter choice: ");

            int choice = readInt();

            switch (choice) {

                case 1:
                    displayInventory(inventory);
                    break;

                case 2:

                    try {

                        System.out.print("Enter Color: ");
                        String color = scanner.nextLine().trim();

                        System.out.print("Enter Size (36-45): ");
                        int size = readInt();

                        System.out.print("Enter Quantity: ");
                        int quantity = readInt();

                        inventory.addStock(color, size, quantity);

                        System.out.println("Stock added successfully!");

                    } catch (IllegalArgumentException e) {

                        System.out.println("Error: " + e.getMessage());
                    }

                    break;

                case 3:
                    managing = false;
                    break;

                default:
                    System.out.println("Invalid choice!");
            }
        }
    }

    // -----------------------------------------
    // DISPLAY INVENTORY
    // -----------------------------------------

    public static void displayInventory(Inventory inventory) {

        System.out.println("\n========== CURRENT STOCK ==========");

        String[] colors = {"BLACK", "WHITE", "BLUE ", "BROWN"};

        for (String color : colors) {

            for (int size = 36; size <= 45; size++) {

                int quantity = inventory.getStock(color, size);

                if (quantity > 0) {

                    System.out.println(
                            color + " | Size: " + size
                                    + " | Stock: " + quantity
                    );
                }
            }
        }
    }

    // -----------------------------------------
    // 05. MANAGE RETURNS
    // -----------------------------------------

    public static void manageReturns(
            List<Order> orders,
            ReturnService returnService,
            Inventory inventory,
            FileStorage fileStorage) {

        boolean managing = true;

        while (managing) {
            System.out.println("\n========== RETURN MANAGEMENT ==========");
            System.out.println("1. View Returned Orders");
            System.out.println("2. Process Returned Order");
            System.out.println("3. View Restocked Items");
            System.out.println("4. View Damaged Items");
            System.out.println("5. Back to Main Menu");
            System.out.print("Enter choice: ");

            int choice = readInt();

            switch (choice) {
                case 1:
                    viewReturnedOrders(orders, returnService);
                    break;
                case 2:
                    processReturnedOrder(orders, returnService, inventory, fileStorage);
                    break;
                case 3:
                    viewReturnItems(orders, true);
                    break;
                case 4:
                    viewReturnItems(orders, false);
                    break;
                case 5:
                    managing = false;
                    break;
                default:
                    System.out.println("Invalid choice!");
            }
        }
    }

    public static void viewReturnedOrders(
            List<Order> orders, ReturnService returnService) {

        System.out.println("\n========== RETURNED ORDERS ==========");
        int count = 0;

        for (Order order : orders) {
            if (order.getStatus() == OrderStatus.RETURNED) {
                count++;
                String state = returnService.isOrderProcessed(order.getOrderId())
                        ? "PROCESSED" : "NOT PROCESSED";
                System.out.println("Order ID: " + order.getOrderId()
                        + " | Customer: " + order.getCustomerName()
                        + " | District: " + order.getDistrict()
                        + " | " + state);
            }
        }

        if (count == 0) {
            System.out.println("No returned orders available.");
        }
        System.out.println("Total Returned Orders: " + count);
    }

    public static void processReturnedOrder(
            List<Order> orders,
            ReturnService returnService,
            Inventory inventory,
            FileStorage fileStorage) {

        System.out.println("\n========== PROCESS RETURNED ORDER ==========");
        System.out.print("Enter Order ID: ");

        int orderId = readInt();
        Order order = findOrderById(orders, orderId);

        if (order == null || order.getStatus() != OrderStatus.RETURNED) {
            System.out.println("Returned order not found!");
            return;
        }

        if (returnService.isOrderProcessed(orderId)) {
            System.out.println("This order has already been processed.");
            return;
        }

        List<OrderItem> items = order.getItems();
        List<Integer> goodQuantities = new ArrayList<>();
        List<Integer> damagedQuantities = new ArrayList<>();

        System.out.println("\nOrder ID: " + order.getOrderId());
        System.out.println("Customer: " + order.getCustomerName());

        for (int i = 0; i < items.size(); i++) {
            OrderItem item = items.get(i);

            System.out.println("\nItem " + (i + 1) + ": "
                    + item.getColor() + " | Size: " + item.getSize()
                    + " | Returned Quantity: " + item.getQuantity());

            int good;
            int damaged;

            while (true) {
                System.out.print("Good Condition Quantity: ");
                good = readInt();
                System.out.print("Damaged Quantity: ");
                damaged = readInt();

                if (good >= 0 && damaged >= 0
                        && good + damaged == item.getQuantity()) {
                    break;
                }

                System.out.println("Good + Damaged must equal "
                        + item.getQuantity() + ". Please try again.");
            }

            goodQuantities.add(good);
            damagedQuantities.add(damaged);
        }

        int totalGood = 0;
        int totalDamaged = 0;
        double damagedLoss = 0;

        for (int i = 0; i < items.size(); i++) {
            totalGood += goodQuantities.get(i);
            totalDamaged += damagedQuantities.get(i);
            damagedLoss += damagedQuantities.get(i) * items.get(i).getUnitPrice();
        }

        System.out.println("\n========== RETURN SUMMARY ==========");
        System.out.println("Total Returned Shoes : " + order.getTotalQuantity());
        System.out.println("Good Condition Shoes : " + totalGood);
        System.out.println("Damaged Shoes        : " + totalDamaged);
        System.out.println("Stock to be Added    : " + totalGood);
        System.out.println("Damaged Product Loss : Rs. " + damagedLoss);
        System.out.println("Delivery Loss        : Rs. " + order.getDeliveryCharge());
        System.out.println("Total Return Loss    : Rs. " + (damagedLoss + order.getDeliveryCharge()));

        System.out.println("\n1. Confirm Processing");
        System.out.println("2. Cancel");
        System.out.print("Enter choice: ");

        if (readInt() != 1) {
            System.out.println("Return processing cancelled. No changes made.");
            return;
        }

        try {
            returnService.processReturnedOrder(order, goodQuantities, damagedQuantities);
            saveData(orders, inventory, fileStorage, returnService);
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static void viewReturnItems(List<Order> orders, boolean showGoodItems) {

        System.out.println(showGoodItems
                ? "\n========== RESTOCKED ITEMS =========="
                : "\n========== DAMAGED ITEMS ==========");

        int totalQuantity = 0;

        for (Order order : orders) {
            if (order.getStatus() != OrderStatus.RETURNED) {
                continue;
            }

            for (OrderItem item : order.getItems()) {
                int quantity = showGoodItems
                        ? item.getGoodConditionQuantity()
                        : item.getDamagedQuantity();

                if (quantity > 0) {
                    totalQuantity += quantity;
                    System.out.println("Order ID: " + order.getOrderId()
                            + " | " + item.getColor()
                            + " | Size: " + item.getSize()
                            + " | Quantity: " + quantity);
                }
            }
        }

        if (totalQuantity == 0) {
            System.out.println("No items available.");
        }
        System.out.println("Total Quantity: " + totalQuantity);
    }

    // -----------------------------------------
    // 06. FINANCIAL REPORT WITH PERIOD SELECTION

    public static void financialReportMenu(
            List<Order> orders, FinancialService financialService) {

        boolean viewing = true;
        while (viewing) {
            System.out.println("\n========== FINANCIAL REPORT PERIOD ==========");
            displayPeriodOptions();
            int choice = readInt();

            if (choice == 5) {
                viewing = false;
            } else if (choice >= 1 && choice <= 4) {
                LocalDate[] period = getReportPeriod(choice);
                displayFinancialReport(
                        orders, financialService, period[0], period[1], choice);
            } else {
                System.out.println("Invalid menu choice!");
            }
        }
    }

    public static void displayFinancialReport(
            List<Order> orders, FinancialService financialService,
            LocalDate startDate, LocalDate endDate, int periodChoice) {

        System.out.println("\n========== FINANCIAL REPORT ==========");
        displaySelectedPeriod(periodChoice, startDate, endDate);

        double totalProfit =
                financialService.calculateTotalProfit(orders, startDate, endDate);
        double deliveryLoss =
                financialService.calculateDeliveryLoss(orders, startDate, endDate);
        double damagedProductLoss =
                financialService.calculateDamagedProductLoss(orders, startDate, endDate);
        double totalLoss =
                financialService.calculateTotalLoss(orders, startDate, endDate);
        double netProfit =
                financialService.calculateNetProfit(orders, startDate, endDate);

        System.out.println("Total Profit         : Rs. " + totalProfit);
        System.out.println("Delivery Loss        : Rs. " + deliveryLoss);
        System.out.println("Damaged Product Loss : Rs. " + damagedProductLoss);
        System.out.println("Total Loss           : Rs. " + totalLoss);
        System.out.println("----------------------------------");
        System.out.println("Net Profit           : Rs. " + netProfit);

    }

    // 07. SALES REPORTS WITH PERIOD SELECTION

    public static void salesReportMenu(List<Order> orders) {

        boolean viewing = true;
        while (viewing) {
            System.out.println("\n========== SALES REPORT PERIOD ==========");
            displayPeriodOptions();
            int choice = readInt();

            if (choice == 5) {
                viewing = false;
            } else if (choice >= 1 && choice <= 4) {
                LocalDate[] period = getReportPeriod(choice);
                List<Order> filteredOrders =
                        filterCompletedSalesByDate(orders, period[0], period[1]);

                displaySalesReports(
                        filteredOrders, choice, period[0], period[1]);

            } else {
                System.out.println("Invalid menu choice!");
            }
        }
    }

    public static void displaySalesReports(
            List<Order> orders, int periodChoice,
            LocalDate startDate, LocalDate endDate) {

        System.out.println("\n========== SALES REPORTS ==========");
        displaySelectedPeriod(periodChoice, startDate, endDate);

        if (orders.isEmpty()) {
            System.out.println("No orders available for this period.");
            return;
        }

        Report[] reports = {
                new DistrictReport(),
                new ColorReport(),
                new SizeReport()
        };

        for (Report report : reports) {
            report.generateReport(orders);
            System.out.println("----------------------------------");
        }
    }

    private static void displayPeriodOptions() {
        System.out.println("1. All Time");
        System.out.println("2. Last Month");
        System.out.println("3. Last 3 Months");
        System.out.println("4. Last Year");
        System.out.println("5. Back to Main Menu");
        System.out.print("Enter your choice: ");
    }

    // Null dates represent All Time.
    private static LocalDate[] getReportPeriod(int choice) {
        LocalDate today = LocalDate.now();
        YearMonth currentMonth = YearMonth.from(today);

        switch (choice) {
            case 2: {
                YearMonth lastMonth = currentMonth.minusMonths(1);
                return new LocalDate[] {
                        lastMonth.atDay(1), lastMonth.atEndOfMonth()
                };
            }
            case 3: {
                YearMonth lastMonth = currentMonth.minusMonths(1);
                YearMonth firstMonth = currentMonth.minusMonths(3);
                return new LocalDate[] {
                        firstMonth.atDay(1), lastMonth.atEndOfMonth()
                };
            }
            case 4: {
                int lastYear = Year.now().getValue() - 1;
                return new LocalDate[] {
                        LocalDate.of(lastYear, 1, 1),
                        LocalDate.of(lastYear, 12, 31)
                };
            }
            default:
                return new LocalDate[] {null, null};
        }
    }

    private static List<Order> filterCompletedSalesByDate(
            List<Order> orders, LocalDate startDate, LocalDate endDate) {

        if (startDate == null && endDate == null) {
            return new ArrayList<>(orders);
        }

        List<Order> filtered = new ArrayList<>();
        for (Order order : orders) {
            if (order.getStatus() != OrderStatus.COMPLETED) {
                continue;
            }

            LocalDate date = order.getCompletedDate();
            if (date != null && !date.isBefore(startDate)
                    && !date.isAfter(endDate)) {
                filtered.add(order);
            }
        }
        return filtered;
    }

    private static void displaySelectedPeriod(
            int choice, LocalDate startDate, LocalDate endDate) {

        String[] names = {
                "", "All Time", "Last Month", "Last 3 Months", "Last Year"
        };
        System.out.println("Report Period: " + names[choice]);

        if (startDate != null && endDate != null) {
            System.out.println("From: " + startDate + "  To: " + endDate);
        }
    }

    // 08. SAVE DATA
    // -----------------------------------------

    public static void saveData(
            List<Order> orders,
            Inventory inventory,
            FileStorage fileStorage,
            ReturnService returnService) {

        try {
            fileStorage.saveOrders(orders);
            fileStorage.saveInventory(inventory);
            fileStorage.saveRestockedOrderIds(
                    returnService.getRestockedOrderIds()
            );
            System.out.println("\nSystem data saved successfully!");
        } catch (RuntimeException e) {
            System.out.println("\nERROR: System data could not be saved completely.");
            System.out.println("Details: " + e.getMessage());
        }
    }

    private static double readDouble() {
        while (true) {
            String input = scanner.nextLine().trim();
            try {
                return Double.parseDouble(input);
            } catch (NumberFormatException e) {
                System.out.print("Please enter a valid amount: ");
            }
        }
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
