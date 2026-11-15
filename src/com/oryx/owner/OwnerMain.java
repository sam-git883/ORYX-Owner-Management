package com.oryx.owner;

import com.oryx.Main;
import java.util.Scanner;

public class OwnerMain {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        System.out.println("==================================");
        System.out.println("         ORYX OWNER SYSTEM");
        System.out.println("==================================");

        // Share one Scanner to avoid input-buffer conflicts.
        OwnerLogin ownerLogin = new OwnerLogin(scanner);

        if (ownerLogin.login()) {
            System.out.println("\nOpening Owner Dashboard...");
            Main.setScanner(scanner);
            Main.main(args);
        }
    }
}
