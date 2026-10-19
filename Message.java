import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;


public class Message {

    // ---------- Shared (static) state ----------
    private static final List<Message> sentMessages = new ArrayList<>();
    private static final List<Message> storedMessages = new ArrayList<>();
    private static String storagePath = "storedMessages.json";

    // ---------- Per-message data ----------
    private final String messageID;
    private final int messageNumber;      // 0 for the first message, 1 for the second, ...
    private final String recipient;
    private final String messageText;
    private final String messageHash;

    public Message(String messageID, int messageNumber, String recipient, String messageText) {
        this.messageID = messageID;
        this.messageNumber = messageNumber;
        this.recipient = recipient;
        this.messageText = messageText;
        this.messageHash = createMessageHash();
    }

    // ---------- Auto generation ----------

    /** Randomly generated ten-digit message ID (leading zeros allowed). */
    public static String generateMessageID() {
        String id = "";
        for (int i = 0; i < 10; i++) {
            id += ThreadLocalRandom.current().nextInt(10);
        }
        return id;
    }

    // ---------- Validation ----------

    /** The message ID must be no more than ten characters. */
    public boolean checkMessageID() {
        return messageID != null && messageID.length() <= 10;
    }

    public String getMessageIDStatus() {
        return "Message ID generated: " + messageID;
    }

    /** Recipient must start with an international code (+) and be no more than ten digits after the code. */
    public String checkRecipientCell() {
        if (recipient != null && recipient.matches("^\\+\\d{3,13}$")) {
            return "Cell phone number successfully captured.";
        }
        return "Cell phone number is incorrectly formatted or does not contain an international code. "
                + "Please correct the number and try again.";
    }

    /** Message must not exceed 250 characters. */
    public String checkMessageLength() {
        if (messageText == null) {
            return "Please enter a message of less than 250 characters.";
        }
        int length = messageText.length();
        if (length <= 250) {
            return "Message ready to send.";
        }
        return "Message exceeds 250 characters by " + (length - 250) + ", please reduce the size.";
    }

    // ---------- Hash ----------

    /**
     * First two digits of the ID : message number : first word + last word, all caps.
     * e.g. 00:0:HITONIGHT
     */
    public final String createMessageHash() {
        String firstTwo = (messageID == null) ? "" : messageID.substring(0, Math.min(2, messageID.length()));

        String firstWord = "";
        String lastWord = "";
        if (messageText != null) {
            String[] words = messageText.trim().split("\\s+");
            if (words.length > 0 && !words[0].isEmpty()) {
                firstWord = cleanWord(words[0]);
                lastWord = cleanWord(words[words.length - 1]);
            }
        }
        return (firstTwo + ":" + messageNumber + ":" + firstWord + lastWord).toUpperCase();
    }

    /** Removes punctuation such as the "?" in "tonight?" so only letters/digits remain. */
    private static String cleanWord(String word) {
        return word.replaceAll("[^A-Za-z0-9]", "");
    }

    // ---------- Send / disregard / store ----------

    /**
     * 1 = send, 2 = disregard, 3 = store for later.
     * Returns the message the system should show for that choice.
     */
    public String sentMessage(int choice) {
        switch (choice) {
            case 1:
                sentMessages.add(this);
                return "Message successfully sent.";
            case 2:
                return "Press 0 to delete the message.";
            case 3:
                storeMessage();
                return "Message successfully stored.";
            default:
                return "Invalid option. Please choose 1, 2 or 3.";
        }
    }

    // ---------- Reporting ----------

    /** Full details in the required order: Message ID, Message Hash, Recipient, Message. */
    public String displayDetails() {
        return "Message ID: " + messageID + "\n"
                + "Message Hash: " + messageHash + "\n"
                + "Recipient: " + recipient + "\n"
                + "Message: " + messageText;
    }

    /** All messages sent while the program is running. */
    public static String printMessages() {
        if (sentMessages.isEmpty()) {
            return "No messages have been sent yet.";
        }
        StringBuilder sb = new StringBuilder();
        for (Message m : sentMessages) {
            sb.append(m.displayDetails()).append("\n\n");
        }
        return sb.toString().trim();
    }

    public static int returnTotalMessages() {
        return sentMessages.size();
    }

    // ---------- JSON storage ----------

    /**
     * Adds this message to the stored list and writes every stored message to a JSON file.
     * Written by hand so no extra library has to be installed.
     */
    public void storeMessage() {
        storedMessages.add(this);

        StringBuilder json = new StringBuilder("[\n");
        for (int i = 0; i < storedMessages.size(); i++) {
            Message m = storedMessages.get(i);
            json.append("  {\n")
                .append("    \"messageID\": \"").append(escapeJson(m.messageID)).append("\",\n")
                .append("    \"messageHash\": \"").append(escapeJson(m.messageHash)).append("\",\n")
                .append("    \"recipient\": \"").append(escapeJson(m.recipient)).append("\",\n")
                .append("    \"message\": \"").append(escapeJson(m.messageText)).append("\"\n")
                .append("  }");
            if (i < storedMessages.size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }
        json.append("]\n");

        try {
            Files.write(Paths.get(storagePath), json.toString().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            System.out.println("Could not write the JSON file: " + e.getMessage());
        }
    }

    private static String escapeJson(String text) {
        if (text == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()) {
            switch (c) {
                case '"':  sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n");  break;
                case '\r': sb.append("\\r");  break;
                case '\t': sb.append("\\t");  break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }

    // ---------- Getters / test helpers ----------

    public String getMessageID()   { return messageID; }
    public String getMessageHash() { return messageHash; }
    public String getRecipient()   { return recipient; }
    public String getMessageText() { return messageText; }

    public static void setStoragePath(String path) { storagePath = path; }

    /** Clears the shared lists (used by the unit tests). */
    public static void resetAll() {
        sentMessages.clear();
        storedMessages.clear();
    }
}

