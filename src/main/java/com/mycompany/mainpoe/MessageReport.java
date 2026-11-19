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

/**
 * Part 3: maintains arrays of messages and reads stored messages from JSON.
 * Gson documentation: https://github.com/google/gson/blob/main/UserGuide.md
 * ChatGPT was used to help review and extend this educational project.
 */
public class MessageReport {
    private final ArrayList<Message> sentMessages = new ArrayList<>();
    private final ArrayList<Message> disregardedMessages = new ArrayList<>();
    private final ArrayList<Message> storedMessages = new ArrayList<>();
    private final ArrayList<String> messageHashes = new ArrayList<>();
    private final ArrayList<String> messageIDs = new ArrayList<>();
    private final File jsonFile;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public MessageReport(File jsonFile) throws IOException {
        this.jsonFile = jsonFile;
        loadStoredMessages();
    }

    public MessageReport() throws IOException { this(new File("messages.json")); }

    /** Records only successfully completed actions; no sample data is hard-coded. */
    public void addMessage(Message message, String actionResult) {
        switch (actionResult) {
            case "Message successfully sent.":
                sentMessages.add(message);
                break;
            case "Press 0 to delete the message.":
                disregardedMessages.add(message);
                break;
            case "Message successfully stored.":
                storedMessages.add(message);
                break;
            default:
                return;
        }
        messageIDs.add(message.getMessageID());
        messageHashes.add(message.getMessageHash());
    }

    /** Reload the saved JSON file into the stored-messages array. */
    public final void loadStoredMessages() throws IOException {
        storedMessages.clear();
        if (jsonFile.exists() && jsonFile.length() > 0) {
            try (FileReader reader = new FileReader(jsonFile)) {
                Type type = new TypeToken<ArrayList<Message>>() {}.getType();
                List<Message> loaded = gson.fromJson(reader, type);
                if (loaded != null) storedMessages.addAll(loaded);
            } catch (RuntimeException e) {
                throw new IOException("Unable to read stored messages JSON", e);
            }
        }
        rebuildIndexes();
    }

    private void rebuildIndexes() {
        messageIDs.clear();
        messageHashes.clear();
        for (Message message : allMessages()) {
            messageIDs.add(message.getMessageID());
            messageHashes.add(message.getMessageHash());
        }
    }

    public Message[] getSentMessages() { return sentMessages.toArray(new Message[0]); }
    public Message[] getDisregardedMessages() { return disregardedMessages.toArray(new Message[0]); }
    public Message[] getStoredMessages() { return storedMessages.toArray(new Message[0]); }
    public String[] getMessageHashes() { return messageHashes.toArray(new String[0]); }
    public String[] getMessageIDs() { return messageIDs.toArray(new String[0]); }

    public Message[] allMessages() {
        ArrayList<Message> all = new ArrayList<>();
        all.addAll(sentMessages);
        all.addAll(disregardedMessages);
        all.addAll(storedMessages);
        return all.toArray(new Message[0]);
    }

    public String senderAndRecipients(String sender) {
        if (storedMessages.isEmpty()) return "No stored messages.";
        StringBuilder result = new StringBuilder();
        for (Message message : storedMessages) {
            result.append("Sender: ").append(sender)
                    .append(" | Recipient: ").append(message.getRecipient()).append('\n');
        }
        return result.toString();
    }

    /** The rubric's example is the longest stored message. */
    public Message longestStoredMessage() {
        Message longest = null;
        for (Message message : storedMessages) {
            if (longest == null || message.getMessage().length() > longest.getMessage().length()) {
                longest = message;
            }
        }
        return longest;
    }

    /** Search all statuses so an ID from a sent message is also found. */
    public Message findByID(String id) {
        for (Message message : allMessages()) {
            if (message.getMessageID().equals(id)) return message;
        }
        return null;
    }

    /** Includes both sent and stored messages, as required by the unit-test table. */
    public Message[] findByRecipient(String recipient) {
        ArrayList<Message> matches = new ArrayList<>();
        for (Message message : sentMessages) {
            if (message.getRecipient().equals(recipient)) matches.add(message);
        }
        for (Message message : storedMessages) {
            if (message.getRecipient().equals(recipient)) matches.add(message);
        }
        return matches.toArray(new Message[0]);
    }

    /** Remove a stored message by hash and save the updated JSON file. */
    public String deleteByHash(String hash) throws IOException {
        for (int i = 0; i < storedMessages.size(); i++) {
            Message message = storedMessages.get(i);
            if (message.getMessageHash().equals(hash)) {
                storedMessages.remove(i);
                try {
                    saveStoredMessages();
                } catch (IOException error) {
                    storedMessages.add(i, message);
                    throw error;
                }
                rebuildIndexes();
                return "Message: \"" + message.getMessage() + "\" successfully deleted.";
            }
        }
        return "No stored message found with that hash.";
    }

    private void saveStoredMessages() throws IOException {
        try (FileWriter writer = new FileWriter(jsonFile)) {
            gson.toJson(storedMessages, writer);
        }
    }

    public String storedReport() {
        if (storedMessages.isEmpty()) return "No stored messages.";
        StringBuilder result = new StringBuilder("===== STORED MESSAGES REPORT =====\n");
        for (Message message : storedMessages) {
            result.append(message.printMessageDetails()).append("\n\n");
        }
        return result.toString();
    }

    public String sentReport() {
        if (sentMessages.isEmpty()) return "No sent messages.";
        StringBuilder result = new StringBuilder("===== SENT MESSAGES REPORT =====\n");
        for (Message message : sentMessages) {
            result.append(message.printMessageDetails()).append("\n\n");
        }
        return result.toString();
    }
}
