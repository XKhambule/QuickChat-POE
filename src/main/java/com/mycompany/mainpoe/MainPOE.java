package com.mycompany.mainpoe;

import java.io.IOException;
import java.util.HashSet;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

/** Complete console-based QuickChat PoE: Parts 1, 2 and 3. */
public class MainPOE {
    public static void main(String[] args) {
        try (Scanner input = new Scanner(System.in)) {
            System.out.println("CHAT APPLICATION");
            Login login = new Login();
            System.out.println("\n========== USER REGISTRATION ==========");
            System.out.print("Enter First Name: ");
            String firstName = input.nextLine();
            System.out.print("Enter Last Name: ");
            String lastName = input.nextLine();
            System.out.print("Enter Username: ");
            String username = input.nextLine();
            System.out.print("Enter Password: ");
            String password = input.nextLine();
            System.out.print("Enter South African Cell Number (+27...): ");
            String cellPhone = input.nextLine();
            String registration = login.registerUser(firstName, lastName, username, password, cellPhone);
            System.out.println(registration);
            if (!registration.equals("User has been registered successfully.")) return;

            System.out.println("\n========== LOGIN ==========");
            System.out.print("Enter Username: ");
            String loginUsername = input.nextLine();
            System.out.print("Enter Password: ");
            String loginPassword = input.nextLine();
            boolean loggedIn = login.loginUser(loginUsername, loginPassword);
            System.out.println(login.returnLoginStatus(loggedIn));
            if (!loggedIn) return;

            MessageReport report;
            try {
                report = new MessageReport();
            } catch (IOException e) {
                System.out.println("Unable to load messages.json: " + e.getMessage());
                return;
            }
            System.out.println("\nWelcome to QuickChat.");
            int maximumMessages = readPositiveNumber(input);
            int messageCount = 0;
            Random random = new Random();
            Set<String> usedIDs = new HashSet<>();
            for (String id : report.getMessageIDs()) usedIDs.add(id);
            boolean running = true;
            while (running) {
                System.out.println("\n========== QUICKCHAT MENU ==========");
                System.out.println("1. Send Messages");
                System.out.println("2. Show recently sent messages");
                System.out.println("3. Quit");
                System.out.println("4. Stored Messages");
                System.out.print("Enter your choice: ");
                String choice = input.nextLine().trim();
                switch (choice) {
                    case "1":
                        if (messageCount >= maximumMessages) {
                            System.out.println("You have reached your message limit.");
                            break;
                        }
                        for (int i = messageCount; i < maximumMessages; i++) {
                            String id;
                            do {
                                StringBuilder digits = new StringBuilder();
                                for (int j = 0; j < 10; j++) digits.append(random.nextInt(10));
                                id = digits.toString();
                            } while (!usedIDs.add(id));
                            System.out.println("Message ID generated: " + id);
                            String recipient;
                            while (true) {
                                System.out.print("Enter recipient (+27 followed by 9 digits): ");
                                recipient = input.nextLine().trim();
                                String response = new Message(id, i, recipient, "Test").checkRecipientCell();
                                System.out.println(response);
                                if (response.equals("Cell phone number successfully captured.")) break;
                            }
                            String body;
                            while (true) {
                                System.out.print("Enter your message: ");
                                body = input.nextLine();
                                if (body.trim().isEmpty()) {
                                    System.out.println("Message cannot be empty.");
                                    continue;
                                }
                                String response = new Message(id, i, recipient, body).checkMessageLength();
                                System.out.println(response);
                                if (response.equals("Message ready to send.")) break;
                            }
                            Message message = new Message(id, i, recipient, body);
                            String action = message.SentMessage(input);
                            System.out.println(action);
                            if (action.equals("Message successfully sent.")) {
                                System.out.println(message.printMessageDetails());
                            }
                            report.addMessage(message, action);
                            messageCount++;
                        }
                        System.out.println("Total messages successfully sent: " + Message.getTotalMessagesSent());
                        break;
                    case "2":
                        System.out.println(report.sentReport());
                        break;
                    case "3":
                        running = false;
                        System.out.println("Total messages successfully sent: " + Message.getTotalMessagesSent());
                        System.out.println("Goodbye!");
                        break;
                    case "4":
                        storedMenu(input, report, firstName + " " + lastName);
                        break;
                    default:
                        System.out.println("Invalid choice. Enter 1, 2, 3 or 4.");
                }
            }
        }
    }

    private static void storedMenu(Scanner input, MessageReport report, String sender) {
        while (true) {
            System.out.println("\n========== STORED MESSAGES ==========");
            System.out.println("1. Display senders and recipients");
            System.out.println("2. Display longest stored message");
            System.out.println("3. Search by message ID");
            System.out.println("4. Search by recipient");
            System.out.println("5. Delete stored message by hash");
            System.out.println("6. Display full stored-message report");
            System.out.println("7. Back to main menu");
            System.out.print("Choose an option: ");
            switch (input.nextLine().trim()) {
                case "1":
                    System.out.println(report.senderAndRecipients(sender));
                    break;
                case "2":
                    Message longest = report.longestStoredMessage();
                    System.out.println(longest == null ? "No stored messages." : longest.printMessageDetails());
                    break;
                case "3":
                    System.out.print("Enter message ID: ");
                    Message found = report.findByID(input.nextLine().trim());
                    System.out.println(found == null ? "Message ID not found." : found.printMessageDetails());
                    break;
                case "4":
                    System.out.print("Enter recipient number: ");
                    Message[] matches = report.findByRecipient(input.nextLine().trim());
                    if (matches.length == 0) System.out.println("No messages found for that recipient.");
                    for (Message match : matches) System.out.println(match.printMessageDetails());
                    break;
                case "5":
                    System.out.print("Enter message hash to delete: ");
                    try {
                        System.out.println(report.deleteByHash(input.nextLine().trim()));
                    } catch (IOException e) {
                        System.out.println("Could not update JSON file: " + e.getMessage());
                    }
                    break;
                case "6":
                    System.out.println(report.storedReport());
                    break;
                case "7":
                    return;
                default:
                    System.out.println("Invalid option. Enter a number from 1 to 7.");
            }
        }
    }

    private static int readPositiveNumber(Scanner input) {
        while (true) {
            System.out.print("How many messages would you like to enter? ");
            try {
                int count = Integer.parseInt(input.nextLine().trim());
                if (count > 0) return count;
            } catch (NumberFormatException e) {
                // Re-ask rather than returning to the menu.
            }
            System.out.println("Please enter a whole number greater than zero.");
        }
    }
}
