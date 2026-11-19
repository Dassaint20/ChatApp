import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Part 3 unit tests (assertEquals).
 *
 * Uses the exact test data from the PROG5121 POE:
 *   1) +27834557896  "Did you get the cake?"                                  Sent
 *   2) +27838884567  "Where are you? You are late! I have asked you to be
 *                     on time."                                               Stored
 *   3) +27834484567  "Yohoooo, I am at your gate."                            Disregard
 *   4) 0838884567    "It is dinner time!"                                     Sent
 *   5) +27838884567  "Ok, I am leaving without you."                          Stored
 *
 * NOTE: if your project uses JUnit 4, replace the imports with
 *   import org.junit.Before;
 *   import org.junit.Test;
 *   import static org.junit.Assert.assertEquals;
 *   import static org.junit.Assert.assertTrue;
 * and change @BeforeEach to @Before.
 */
public class MessageTest {

    private static final String TEXT_1 = "Did you get the cake?";
    private static final String TEXT_2 = "Where are you? You are late! I have asked you to be on time.";
    private static final String TEXT_3 = "Yohoooo, I am at your gate.";
    private static final String TEXT_4 = "It is dinner time!";
    private static final String TEXT_5 = "Ok, I am leaving without you.";

    @BeforeEach
    public void setUp() {
        Message.resetAll();
        Message.setStoragePath("test_storedMessages.json");
    }

    /** Populates the arrays with the five test-data messages (flags 1=Sent, 2=Disregard, 3=Stored). */
    private Message[] populateTestData() {
        Message m1 = new Message("1234567890", 0, "+27834557896", TEXT_1);
        m1.sentMessage(1);                       // Sent
        Message m2 = new Message("2234567891", 1, "+27838884567", TEXT_2);
        m2.sentMessage(3);                       // Stored
        Message m3 = new Message("3234567892", 2, "+27834484567", TEXT_3);
        m3.sentMessage(2);                       // Disregard
        Message m4 = new Message("4234567893", 3, "0838884567", TEXT_4);
        m4.sentMessage(1);                       // Sent
        Message m5 = new Message("5234567894", 4, "+27838884567", TEXT_5);
        m5.sentMessage(3);                       // Stored
        return new Message[]{m1, m2, m3, m4, m5};
    }

    @Test
    public void testSentMessagesArrayCorrectlyPopulated() {
        populateTestData();
        Message[] sent = Message.getSentMessages();
        assertEquals(2, sent.length);
        assertEquals(TEXT_1, sent[0].getMessageText());
        assertEquals(TEXT_4, sent[1].getMessageText());
    }

    @Test
    public void testAllArraysArePopulated() {
        populateTestData();
        assertEquals(1, Message.getDisregardedMessages().length);
        assertEquals(TEXT_3, Message.getDisregardedMessages()[0].getMessageText());
        assertEquals(2, Message.getStoredMessages().length);
        assertEquals(5, Message.getMessageIDs().length);
        assertEquals(5, Message.getMessageHashes().length);
    }

    @Test
    public void testMessageHashFormat() {
        Message m1 = new Message("1234567890", 0, "+27834557896", TEXT_1);
        // first 2 chars of ID : message number : first word + last word (letters/digits only), upper case
        assertEquals("12:0:DIDCAKE", m1.getMessageHash());
    }

    @Test
    public void testDisplayLongestStoredMessage() {
        populateTestData();
        assertEquals(TEXT_2, Message.displayLongestStoredMessage());
    }

    @Test
    public void testDisplaySenderAndRecipient() {
        Message m = new Message("1234567890", 0, "+27834557896", TEXT_1);
        m.sentMessage(3);                        // Stored
        assertEquals("Sender:  | Recipient: +27834557896", Message.displaySenderAndRecipient());
    }

    @Test
    public void testSearchByMessageId() {
        Message[] data = populateTestData();
        String result = Message.searchByMessageId(data[3].getMessageID());
        assertEquals("Recipient: 0838884567\nMessage: " + TEXT_4, result);
    }

    @Test
    public void testSearchByRecipient() {
        populateTestData();
        String expected = "\"" + TEXT_2 + "\"\n\"" + TEXT_5 + "\"";
        assertEquals(expected, Message.searchByRecipient("+27838884567"));
    }

    @Test
    public void testDeleteMessageUsingMessageHash() {
        Message[] data = populateTestData();
        String result = Message.deleteMessageByHash(data[1].getMessageHash());
        assertEquals("Message: \"" + TEXT_2 + "\" successfully deleted.", result);
        assertEquals(1, Message.getStoredMessages().length);
        assertEquals(TEXT_5, Message.getStoredMessages()[0].getMessageText());
    }

    @Test
    public void testDisplayReport() {
        populateTestData();
        String report = Message.displayReport();
        assertTrue(report.contains("Message Hash"));
        assertTrue(report.contains("Recipient"));
        assertTrue(report.contains(TEXT_2));
        assertTrue(report.contains(TEXT_5));
    }

    @Test
    public void testReadJsonFileBackIntoArray() {
        populateTestData();                      // stores messages 2 and 5 -> writes the JSON file
        Message.resetAll();
        int loaded = Message.loadStoredMessages();
        assertEquals(2, loaded);
        assertEquals(2, Message.getStoredMessages().length);
        assertEquals(TEXT_2, Message.displayLongestStoredMessage());
    }
}

  
