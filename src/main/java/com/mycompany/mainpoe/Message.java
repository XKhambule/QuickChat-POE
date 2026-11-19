package com.mycompany.mainpoe;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

/**
 * Part 2 QuickChat message model and JSON storage.
 * JSON implementation adapted with guidance from Google's Gson User Guide:
 * https://github.com/google/gson/blob/main/UserGuide.md
 * and Gson API documentation: https://javadoc.io/doc/com.google.code.gson/gson/latest/
 * The project also used ChatGPT assistance to develop and revise the code.
 */
//I have improved the hashes and also added extra testing cases//
public class Message {
    private final String messageID;
    private final int messageNumber;
    private final String recipient;
    private final String message;
    private String messageHash;
    private boolean sent;
    private boolean stored;
    private boolean disregarded;
    private static int totalMessagesSent = 0;
    private static final List<Message> sentMessages = new ArrayList<>();

    public Message(String messageID, int messageNumber, String recipient, String message) {
        this.messageID = messageID;
        this.messageNumber = messageNumber;
        this.recipient = recipient;
        this.message = message;
    }

    public boolean checkMessageID() {
        return messageID != null && messageID.matches("[0-9]{10}");
    }

    public String checkRecipientCell() {
        if (recipient != null && recipient.matches("\\+27[0-9]{9}")) {
            return "Cell phone number successfully captured.";
        }
        return "Cell phone number is incorrectly formatted or does not contain "
                + "an international code. Please correct the number and try again.";
    }

    public String checkMessageLength() {
        if (message != null && message.length() <= 250) {
            return "Message ready to send.";
        }
        int extra = message == null ? 0 : Math.max(0, message.length() - 250);
        return "Message exceeds 250 characters by " + extra + "; please reduce the size.";
    }

    private boolean isValidMessage() {
        return checkMessageID()
                && checkRecipientCell().equals("Cell phone number successfully captured.")
                && message != null && !message.trim().isEmpty()
                && checkMessageLength().equals("Message ready to send.");
    }

    public String createMessageHash() {
        if (!checkMessageID() || message == null || message.trim().isEmpty()) {
            return "";
        }
        String[] words = message.trim().split("\\s+");
        // Remove punctuation from the selected words, not the hash separators.
        String first = words[0].replaceAll("[^\\p{L}\\p{N}]", "").toUpperCase(Locale.ROOT);
        String last = words[words.length - 1].replaceAll("[^\\p{L}\\p{N}]", "").toUpperCase(Locale.ROOT);
        messageHash = messageID.substring(0, 2) + ":" + messageNumber + ":" + first + last;
        return messageHash;
    }

    /** Interactive version: reuse the Scanner from MainPOE. */
    public String SentMessage(Scanner input) {
        while (true) {
            System.out.println("\n1. Send Message");
            System.out.println("2. Disregard Message");
            System.out.println("3. Store Message");
            System.out.print("Choose an action: ");
            String choice = input.nextLine().trim();
            if (!choice.equals("1") && !choice.equals("2") && !choice.equals("3")) {
                System.out.println("Invalid choice. Please enter 1, 2 or 3.");
                continue;
            }
            if (choice.equals("2")) {
                System.out.println("Press 0 to delete the message.");
                while (true) {
                    System.out.print("Enter 0 to confirm: ");
                    if (input.nextLine().trim().equals("0")) {
                        return SentMessage("2");
                    }
                    System.out.println("Invalid input. Press 0 to confirm.");
                }
            }
            return SentMessage(choice);
        }
    }

    /** Keep the no-argument method required by the Part 2 specification. */
    public String SentMessage() {
        return SentMessage(new Scanner(System.in));
    }

    /** Non-interactive overload for reliable JUnit tests. */
    public String SentMessage(String choice) {
        if (sent || stored || disregarded) {
            return "This message has already been processed.";
        }
        switch (choice) {
            case "1":
                if (!isValidMessage()) {
                    return "Message cannot be sent. Please check the message details.";
                }
                createMessageHash();
                sent = true;
                totalMessagesSent++;
                sentMessages.add(this);
                return "Message successfully sent.";
            case "2":
                disregarded = true;
                return "Press 0 to delete the message.";
            case "3":
                return storeMessage();
            default:
                return "Invalid choice. Please try again.";
        }
    }

    public String storeMessage() {
        return storeMessage(new File("messages.json"));
    }

    /** File overload lets tests write into a temporary folder. */
    public String storeMessage(File file) {
        if (!isValidMessage()) {
            return "Message cannot be stored. Please check the message details.";
        }
        if (sent || stored || disregarded) {
            return "This message has already been processed.";
        }
        createMessageHash();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        List<Message> messages = new ArrayList<>();
        if (file.exists() && file.length() > 0) {
            try (FileReader reader = new FileReader(file)) {
                Type type = new TypeToken<ArrayList<Message>>() {}.getType();
                List<Message> previous = gson.fromJson(reader, type);
                if (previous != null) messages.addAll(previous);
            } catch (IOException | RuntimeException e) {
                return "Error reading JSON file: " + e.getMessage();
            }
        }
        messages.add(this);
        try (FileWriter writer = new FileWriter(file)) {
            stored = true;
            gson.toJson(messages, writer);
            return "Message successfully stored.";
        } catch (IOException e) {
            stored = false;
            return "Error storing message: " + e.getMessage();
        }
    }

    public String printMessageDetails() {
        if (messageHash == null) createMessageHash();
        return "\nMessage ID: " + messageID
                + "\nMessage Hash: " + messageHash
                + "\nRecipient: " + recipient
                + "\nMessage: " + message;
    }

    /** Display all messages successfully sent during this run. */
    public String printMessages() {
        if (sentMessages.isEmpty()) return "No messages have been sent.";
        StringBuilder details = new StringBuilder();
        for (Message current : sentMessages) {
            details.append(current.printMessageDetails()).append('\n');
        }
        return details.toString();
    }

    // Part 3: getters allow arrays and reports to read the message details.
    public String getMessageID() { return messageID; }
    public int getMessageNumber() { return messageNumber; }
    public String getRecipient() { return recipient; }
    public String getMessage() { return message; }
    public String getMessageHash() { return createMessageHash(); }
    public boolean isSent() { return sent; }
    public boolean isStored() { return stored; }
    public boolean isDisregarded() { return disregarded; }

    public static int getTotalMessagesSent() {
        return totalMessagesSent;
    }

    public int returnTotalMessagess() {
        return totalMessagesSent;
    }
}
