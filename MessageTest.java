import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;

public class MessageTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    // Test Data for Message 1 (sent)
    private static final String RECIPIENT_1 = "+27718693002";
    private static final String TEXT_1 = "Hi Mike, can you join us for dinner tonight?";

    // Test Data for Message 2 (discarded)
    private static final String RECIPIENT_2 = "08575975889";
    private static final String TEXT_2 = "Hi Keegan, did you receive the payment?";

    private Message message1;
    private Message message2;

    @Before
    public void setUp() throws Exception {
        Message.resetAll();
        Message.setStoragePath(tempFolder.newFile("storedMessages.json").getAbsolutePath());
        message1 = new Message("0012345678", 0, RECIPIENT_1, TEXT_1);
        message2 = new Message("0987654321", 1, RECIPIENT_2, TEXT_2);
    }

    // ---------- Message length (assertEquals) ----------

    @Test
    public void testMessageLength_Success() {
        assertEquals("Message ready to send.", message1.checkMessageLength());
    }

    @Test
    public void testMessageLength_Failure() {
        String longText = new String(new char[260]).replace('\0', 'a');   // 10 over the limit
        Message tooLong = new Message("0012345678", 0, RECIPIENT_1, longText);
        assertEquals("Message exceeds 250 characters by 10, please reduce the size.",
                tooLong.checkMessageLength());
    }

    // ---------- Recipient number (assertEquals) ----------

    @Test
    public void testRecipientNumber_Success() {
        assertEquals("Cell phone number successfully captured.", message1.checkRecipientCell());
    }

    @Test
    public void testRecipientNumber_Failure() {
        assertEquals("Cell phone number is incorrectly formatted or does not contain an "
                + "international code. Please correct the number and try again.",
                message2.checkRecipientCell());
    }

    // ---------- Message hash (assertEquals) ----------

    @Test
    public void testMessageHash_TestCase1() {
        assertEquals("00:0:HITONIGHT", message1.createMessageHash());
        assertEquals("00:0:HITONIGHT", message1.getMessageHash());
    }

    @Test
    public void testMessageHash_LoopOverRemainingHashes() {
        String[] ids = {"0012345678", "0987654321", "5544332211", "1234567890"};
        String[] texts = {
            "Hi Mike, can you join us for dinner tonight?",
            "Hi Keegan, did you receive the payment?",
            "Hello there",
            "Thanks"
        };
        String[] expected = {
            "00:0:HITONIGHT",
            "09:1:HIPAYMENT",
            "55:2:HELLOTHERE",
            "12:3:THANKSTHANKS"
        };

        for (int i = 0; i < texts.length; i++) {
            Message m = new Message(ids[i], i, RECIPIENT_1, texts[i]);
            assertEquals("Hash " + i, expected[i], m.createMessageHash());
        }
    }

    // ---------- Message ID (assertEquals / assertTrue) ----------

    @Test
    public void testMessageID_IsCreated() {
        String id = Message.generateMessageID();
        Message m = new Message(id, 0, RECIPIENT_1, TEXT_1);
        assertEquals("Message ID generated: " + id, m.getMessageIDStatus());
    }

    @Test
    public void testMessageID_NotMoreThanTenCharacters() {
        assertEquals(10, Message.generateMessageID().length());
        assertTrue(message1.checkMessageID());
        assertFalse(new Message("12345678901", 0, RECIPIENT_1, TEXT_1).checkMessageID());
    }

    // ---------- MessageSent: send / disregard / store ----------

    @Test
    public void testSentMessage_Send() {
        assertEquals("Message successfully sent.", message1.sentMessage(1));
    }

    @Test
    public void testSentMessage_Disregard() {
        assertEquals("Press 0 to delete the message.", message2.sentMessage(2));
    }

    @Test
    public void testSentMessage_Store() {
        assertEquals("Message successfully stored.", message1.sentMessage(3));
    }

    // ---------- Totals and printing ----------

    @Test
    public void testReturnTotalMessages_SendOneDiscardOne() {
        message1.sentMessage(1);    // sent
        message2.sentMessage(2);    // discarded - must not be counted
        assertEquals(1, Message.returnTotalMessages());
    }

    @Test
    public void testPrintMessages_ShowsSentMessages() {
        assertEquals("No messages have been sent yet.", Message.printMessages());
        message1.sentMessage(1);
        String printed = Message.printMessages();
        assertTrue(printed.contains("Message ID: 0012345678"));
        assertTrue(printed.contains("Message Hash: 00:0:HITONIGHT"));
        assertTrue(printed.contains("Recipient: +27718693002"));
        assertTrue(printed.contains("Message: " + TEXT_1));
    }

    @Test
    public void testDisplayDetails_Order() {
        assertEquals("Message ID: 0012345678\n"
                + "Message Hash: 00:0:HITONIGHT\n"
                + "Recipient: +27718693002\n"
                + "Message: " + TEXT_1, message1.displayDetails());
    }

    // ---------- JSON storage ----------

    @Test
    public void testStoreMessage_WritesJsonFile() throws Exception {
        message1.sentMessage(3);
        File file = new File(tempFolder.getRoot(), "storedMessages.json");
        String json = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
        assertTrue(json.contains("\"messageID\": \"0012345678\""));
        assertTrue(json.contains("\"messageHash\": \"00:0:HITONIGHT\""));
        assertTrue(json.contains("\"recipient\": \"+27718693002\""));
        assertTrue(json.contains("\"message\": \"" + TEXT_1 + "\""));
    }
}
