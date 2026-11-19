package com.mycompany.mainpoe;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

/** Part 3 tests use the five sample messages in the PoE, not hard-coded app data. */
public class TestMessageReport {
    @TempDir Path temporaryFolder;
    private MessageReport report;
    private File file;
    private Message m1, m2, m3, m4, m5;

    @BeforeEach void setup() throws IOException {
        file = temporaryFolder.resolve("messages.json").toFile();
        report = new MessageReport(file);
        m1 = new Message("0012345678", 0, "+27834557896", "Did you get the cake?");
        m2 = new Message("0112345678", 1, "+27838884567", "Where are you? You are late! I have asked you to be on time.");
        m3 = new Message("0212345678", 2, "+27834484567", "Yohoooo, I am at your gate.");
        // The assignment supplies a local-format recipient for message 4.
        // We populate the array directly for this test; live input still requires +27.
        m4 = new Message("0312345678", 3, "0838884567", "It is dinner time !");
        m5 = new Message("0412345678", 4, "+27838884567", "Ok, I am leaving without you.");
        report.addMessage(m1, "Message successfully sent.");
        report.addMessage(m2, "Message successfully stored.");
        report.addMessage(m3, "Press 0 to delete the message.");
        report.addMessage(m4, "Message successfully sent.");
        report.addMessage(m5, "Message successfully stored.");
    }

    @Test void sentArrayMatchesPoETestData() {
        assertArrayEquals(new String[]{"Did you get the cake?", "It is dinner time !"},
                new String[]{report.getSentMessages()[0].getMessage(), report.getSentMessages()[1].getMessage()});
    }
    @Test void disregardedArrayMatchesPoETestData() {
        assertEquals("Yohoooo, I am at your gate.", report.getDisregardedMessages()[0].getMessage());
    }
    @Test void storedArrayMatchesPoETestData() {
        assertEquals(2, report.getStoredMessages().length);
        assertEquals(m2.getMessage(), report.getStoredMessages()[0].getMessage());
    }
    @Test void longestStoredMessageMatchesPoETestData() {
        assertEquals("Where are you? You are late! I have asked you to be on time.",
                report.longestStoredMessage().getMessage());
    }
    @Test void searchByMessageIDMatchesPoETestData() {
        assertEquals("It is dinner time !", report.findByID(m4.getMessageID()).getMessage());
        assertEquals("0838884567", report.findByID(m4.getMessageID()).getRecipient());
    }
    @Test void searchRecipientMatchesPoETestData() {
        Message[] found = report.findByRecipient("+27838884567");
        assertEquals(2, found.length);
        assertEquals(m2.getMessage(), found[0].getMessage());
        assertEquals(m5.getMessage(), found[1].getMessage());
    }
    @Test void hashesAndIDsArraysArePopulated() {
        assertEquals(5, report.getMessageHashes().length);
        assertEquals(5, report.getMessageIDs().length);
        assertEquals(m1.getMessageHash(), report.getMessageHashes()[0]);
    }
    @Test void deleteByHashUpdatesArray() throws IOException {
        String result = report.deleteByHash(m2.getMessageHash());
        assertTrue(result.contains("successfully deleted"));
        assertEquals(1, report.getStoredMessages().length);
        assertNull(report.findByID(m2.getMessageID()));
    }
    @Test void reportContainsAllRequiredDetails() {
        String output = report.storedReport();
        assertTrue(output.contains("Message Hash:"));
        assertTrue(output.contains("Recipient:"));
        assertTrue(output.contains("Message:"));
        assertTrue(output.contains(m2.getMessage()));
    }
    @Test void senderAndRecipientDisplay() {
        assertTrue(report.senderAndRecipients("Test Student").contains("Sender: Test Student"));
        assertTrue(report.senderAndRecipients("Test Student").contains("+27838884567"));
    }
    @Test void readsJSONIntoArrayAndPersistsDeletion() throws IOException {
        // Use the actual Part 2 JSON writer and reload in a new report instance.
        Message saved1 = new Message("0512345678", 5, "+27838884567", m2.getMessage());
        Message saved2 = new Message("0612345678", 6, "+27838884567", m5.getMessage());
        assertEquals("Message successfully stored.", saved1.storeMessage(file));
        assertEquals("Message successfully stored.", saved2.storeMessage(file));
        MessageReport reloaded = new MessageReport(file);
        assertEquals(2, reloaded.getStoredMessages().length);
        assertEquals(2, reloaded.getMessageIDs().length);
        reloaded.deleteByHash(saved1.getMessageHash());
        assertEquals(1, new MessageReport(file).getStoredMessages().length);
    }
    @Test void unknownIDAndHashAreHandled() throws IOException {
        assertNull(report.findByID("9999999999"));
        assertEquals("No stored message found with that hash.", report.deleteByHash("missing"));
    }
}
