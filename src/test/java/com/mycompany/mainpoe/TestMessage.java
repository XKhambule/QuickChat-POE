package com.mycompany.mainpoe;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

/** Part 2 tests: validation, assignment test data, hashes, actions and JSON. */
public class TestMessage {
    @TempDir Path temporaryFolder;

    private Message sample(String id, int number, String recipient, String text) {
        return new Message(id, number, recipient, text);
    }

    @Test public void testValidMessageID() {
        assertTrue(sample("0012345678", 0, "+27718693002", "Hi tonight").checkMessageID());
    }

    @Test public void testInvalidMessageID() {
        assertFalse(sample("12345", 0, "+27718693002", "Hi tonight").checkMessageID());
    }

    @Test public void testValidRecipientCell() {
        assertEquals("Cell phone number successfully captured.",
                sample("0012345678", 0, "+27718693002", "Hi tonight").checkRecipientCell());
    }

    @Test public void testInvalidRecipientCellFromAssignment() {
        assertEquals("Cell phone number is incorrectly formatted or does not contain "
                + "an international code. Please correct the number and try again.",
                sample("0012345678", 1, "08575975889",
                        "Hi Keegan, did you receive the payment?").checkRecipientCell());
    }

    @Test public void testValidMessageLength() {
        assertEquals("Message ready to send.",
                sample("0012345678", 0, "+27718693002", "A".repeat(250)).checkMessageLength());
    }

    @Test public void testInvalidMessageLength() {
        assertEquals("Message exceeds 250 characters by 1; please reduce the size.",
                sample("0012345678", 0, "+27718693002", "A".repeat(251)).checkMessageLength());
    }

    @Test public void testHashWithoutPunctuation() {
        assertEquals("00:0:HITONIGHT", sample("0012345678", 0,
                "+27718693002", "Hi, tonight?").createMessageHash());
    }

    @Test public void testHashLoop() {
        String[] ids = {"0012345678", "1234567890", "9876543210"};
        String[] texts = {"Hi tonight", "Hello, Mike!", "See you tomorrow."};
        String[] expected = {"00:0:HITONIGHT", "12:1:HELLOMIKE", "98:2:SEETOMORROW"};
        for (int i = 0; i < ids.length; i++) {
            assertEquals(expected[i], sample(ids[i], i,
                    "+27718693002", texts[i]).createMessageHash());
        }
    }

    @Test public void testAssignmentMessageOneSend() {
        Message message = sample("0012345678", 0, "+27718693002",
                "Hi Mike, can you join us for dinner tonight?");
        int before = Message.getTotalMessagesSent();
        assertEquals("Message successfully sent.", message.SentMessage("1"));
        assertEquals(before + 1, Message.getTotalMessagesSent());
        assertTrue(message.printMessageDetails().contains("Hi Mike, can you join us for dinner tonight?"));
    }

    @Test public void testAssignmentMessageTwoDisregard() {
        Message message = sample("1234567890", 1, "08575975889",
                "Hi Keegan, did you receive the payment?");
        int before = Message.getTotalMessagesSent();
        assertEquals("Press 0 to delete the message.", message.SentMessage("2"));
        assertEquals(before, Message.getTotalMessagesSent());
    }

    @Test public void testCannotSendInvalidRecipient() {
        Message message = sample("0012345678", 0, "08575975889", "Hi tonight");
        assertEquals("Message cannot be sent. Please check the message details.",
                message.SentMessage("1"));
    }

    @Test public void testDuplicateActionBlocked() {
        Message message = sample("0012345678", 0, "+27718693002", "Hi tonight");
        assertEquals("Message successfully sent.", message.SentMessage("1"));
        assertEquals("This message has already been processed.", message.SentMessage("1"));
    }

    @Test public void testInvalidAction() {
        assertEquals("Invalid choice. Please try again.",
                sample("0012345678", 0, "+27718693002", "Hi tonight").SentMessage("9"));
    }

    @Test public void testPrintSentMessages() {
        Message message = sample("0012345678", 0, "+27718693002", "A unique test message");
        message.SentMessage("1");
        assertTrue(message.printMessages().contains("A unique test message"));
    }

    @Test public void testJsonStorageCreatesFile() throws IOException {
        File file = temporaryFolder.resolve("messages.json").toFile();
        Message message = sample("0012345678", 0, "+27718693002", "Hi tonight");
        assertEquals("Message successfully stored.", message.storeMessage(file));
        assertTrue(file.exists());
        try (FileReader reader = new FileReader(file)) {
            JsonArray array = JsonParser.parseReader(reader).getAsJsonArray();
            assertEquals(1, array.size());
            assertEquals("Hi tonight", array.get(0).getAsJsonObject()
                    .get("message").getAsString());
        }
    }

    @Test public void testJsonStorageAppends() throws IOException {
        File file = temporaryFolder.resolve("messages.json").toFile();
        assertEquals("Message successfully stored.",
                sample("0012345678", 0, "+27718693002", "First").storeMessage(file));
        assertEquals("Message successfully stored.",
                sample("1234567890", 1, "+27718693002", "Second").storeMessage(file));
        try (FileReader reader = new FileReader(file)) {
            assertEquals(2, JsonParser.parseReader(reader).getAsJsonArray().size());
        }
    }
}
