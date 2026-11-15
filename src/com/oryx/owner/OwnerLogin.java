
package com.oryx.owner;

import java.util.Scanner;

public class OwnerLogin {

    private static final String USERNAME = "admin";
    private static final String PASSWORD = "Admin@123";

    private final Scanner scanner;

    public OwnerLogin(Scanner scanner) {
        this.scanner = scanner;
    }

    public boolean login() {

        System.out.println("\n==================================");
        System.out.println("         ORYX OWNER LOGIN");
        System.out.println("==================================");

        while (true) {

            System.out.print("Enter Username: ");
            String username = scanner.nextLine().trim();

            System.out.print("Enter Password: ");
            String password = scanner.nextLine();

            if (username.equals(USERNAME)
                    && password.equals(PASSWORD)) {

                System.out.println("\nLogin Successful!");
                System.out.println("Welcome to ORYX Owner Management System.");

                return true;

            } else {

                System.out.println("\nInvalid Username or Password!");
                System.out.println("Please try again.\n");
            }
        }
    }
}
