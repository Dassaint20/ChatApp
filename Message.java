import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Message {

    // =====================================================================
    // PART 3 - Arrays that hold every message created in the app.
    // =====================================================================
    private static final List<Message> sentMessages = new ArrayList<>();
    private static final List<Message> disregardedMessages = new ArrayList<>();
    private static final List<Message> storedMessages = new ArrayList<>();
    private static final List<String> messageHashes = new ArrayList<>();
    private static final List<String> messageIDs = new ArrayList<>();
    private static String storagePath = "storedMessages.json";

    private final String messageID;
    private final int messageNumber;      // 0 for the first message, 1 for the second, ...
    private final String sender;          // cell number of the logged-in user
    private final String recipient;
    private final String messageText;
    private final String messageHash;

    public Message(String messageID, int messageNumber, String recipient, String messageText) {
        this(messageID, messageNumber, "", recipient, messageText);
    }

    public Message(String messageID, int messageNumber, String sender, String recipient, String messageText) {
        this.messageID = messageID;
        this.messageNumber = messageNumber;
        this.sender = (sender == null) ? "" : sender;
        this.recipient = recipient;
        this.messageText = messageText;
        this.messageHash = createMessageHash();
    }

    private Message(String messageID, int messageNumber, String sender, String recipient,
                    String messageText, String messageHash) {
        this.messageID = messageID;
        this.messageNumber = messageNumber;
        this.sender = (sender == null) ? "" : sender;
        this.recipient = recipient;
        this.messageText = messageText;
        this.messageHash = messageHash;
    }

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

    public String checkRecipientCell() {
        if (recipient != null && recipient.matches("^\\+\\d{3,13}$")) {
            return "Cell phone number successfully captured.";
        }
        return "Cell phone number is incorrectly formatted or does not contain an international code. "
                + "Please correct the number and try again.";
    }

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

    private static String cleanWord(String word) {
        return word.replaceAll("[^A-Za-z0-9]", "");
    }

    // ---------- Send / disregard / store ----------

    public String sentMessage(int choice) {
        switch (choice) {
            case 1:
                sentMessages.add(this);
                register();
                return "Message successfully sent.";
            case 2:
                disregardedMessages.add(this);
                register();
                return "Press 0 to delete the message.";
            case 3:
                storeMessage();
                return "Message successfully stored.";
            default:
                return "Invalid option. Please choose 1, 2 or 3.";
        }
    }

    /** Adds this message's ID and hash to the message ID and message hash arrays. */
    private void register() {
        messageIDs.add(messageID);
        messageHashes.add(messageHash);
    }

    // ---------- Part 2 reporting ----------

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

    // =====================================================================
    // PART 3 - Array accessors and stored-message features
    // =====================================================================

    /** Sent Message array - contains all the messages that were sent. */
    public static Message[] getSentMessages() {
        return sentMessages.toArray(new Message[0]);
    }

    /** Disregarded Messages array - contains all the messages that were disregarded. */
    public static Message[] getDisregardedMessages() {
        return disregardedMessages.toArray(new Message[0]);
    }

    /** Stored Messages array - populated from the JSON file (see loadStoredMessages). */
    public static Message[] getStoredMessages() {
        return storedMessages.toArray(new Message[0]);
    }

    /** Message Hash array - contains all the message hashes. */
    public static String[] getMessageHashes() {
        return messageHashes.toArray(new String[0]);
    }

    /** Message ID array - contains all the message IDs. */
    public static String[] getMessageIDs() {
        return messageIDs.toArray(new String[0]);
    }

    /** Every message currently held by the app (sent + stored + disregarded). */
    private static List<Message> allMessages() {
        List<Message> all = new ArrayList<>(sentMessages);
        all.addAll(storedMessages);
        all.addAll(disregardedMessages);
        return all;
    }

    // a) Display the sender and recipient of all stored messages.
    public static String displaySenderAndRecipient() {
        if (storedMessages.isEmpty()) {
            return "There are no stored messages.";
        }
        StringBuilder sb = new StringBuilder();
        for (Message m : storedMessages) {
            sb.append("Sender: ").append(m.sender)
              .append(" | Recipient: ").append(m.recipient).append("\n");
        }
        return sb.toString().trim();
    }

    // b) Display the longest stored message.
    public static String displayLongestStoredMessage() {
        if (storedMessages.isEmpty()) {
            return "There are no stored messages.";
        }
        Message longest = storedMessages.get(0);
        for (Message m : storedMessages) {
            if (m.messageText.length() > longest.messageText.length()) {
                longest = m;
            }
        }
        return longest.messageText;
    }

    // c) Search for a message ID and display the corresponding recipient and message.
    public static String searchByMessageId(String id) {
        if (id != null) {
            for (Message m : allMessages()) {
                if (id.equals(m.messageID)) {
                    return "Recipient: " + m.recipient + "\nMessage: " + m.messageText;
                }
            }
        }
        return "Message ID not found.";
    }

    // d) Search for all the messages (sent or stored) for a particular recipient.
    public static String searchByRecipient(String recipient) {
        StringBuilder sb = new StringBuilder();
        int found = 0;
        if (recipient != null) {
            for (Message m : sentMessages) {
                if (recipient.equals(m.recipient)) {
                    sb.append("\"").append(m.messageText).append("\"").append("\n");
                    found++;
                }
            }
            for (Message m : storedMessages) {
                if (recipient.equals(m.recipient)) {
                    sb.append("\"").append(m.messageText).append("\"").append("\n");
                    found++;
                }
            }
        }
        if (found == 0) {
            return "No sent or stored messages found for recipient " + recipient + ".";
        }
        return sb.toString().trim();
    }

    // e) Delete a message using the message hash.
    public static String deleteMessageByHash(String hash) {
        if (hash == null) {
            return "Message hash not found.";
        }
        for (int i = 0; i < storedMessages.size(); i++) {
            Message m = storedMessages.get(i);
            if (m.messageHash.equalsIgnoreCase(hash.trim())) {
                storedMessages.remove(i);
                messageHashes.remove(m.messageHash);
                writeJsonFile();
                return "Message: \"" + m.messageText + "\" successfully deleted.";
            }
        }
        return "Message with hash " + hash + " was not found.";
    }

    // f) Display a report that lists the full details of all the stored messages.
    public static String displayReport() {
        if (storedMessages.isEmpty()) {
            return "There are no stored messages.";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Stored Messages Report\n");
        sb.append("======================\n");
        for (Message m : storedMessages) {
            sb.append("Message ID: ").append(m.messageID).append("\n")
              .append("Message Hash: ").append(m.messageHash).append("\n")
              .append("Sender: ").append(m.sender).append("\n")
              .append("Recipient: ").append(m.recipient).append("\n")
              .append("Message: ").append(m.messageText).append("\n\n");
        }
        return sb.toString().trim();
    }

    // ---------- JSON storage ----------

    public void storeMessage() {
        storedMessages.add(this);
        register();
        writeJsonFile();
    }

    /** Rewrites the whole JSON file from the storedMessages array. */
    private static void writeJsonFile() {
        StringBuilder json = new StringBuilder("[\n");
        for (int i = 0; i < storedMessages.size(); i++) {
            Message m = storedMessages.get(i);
            json.append("  {\n")
                .append("    \"messageID\": \"").append(escapeJson(m.messageID)).append("\",\n")
                .append("    \"messageHash\": \"").append(escapeJson(m.messageHash)).append("\",\n")
                .append("    \"sender\": \"").append(escapeJson(m.sender)).append("\",\n")
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

    /**
     * Reads the JSON file back into the Stored Messages array (and repopulates
     * the Message ID and Message Hash arrays). Returns the number of messages loaded.
     */
    public static int loadStoredMessages() {
        storedMessages.clear();
        java.nio.file.Path path = Paths.get(storagePath);
        if (!Files.exists(path)) {
            return 0;
        }

        String content;
        try {
            content = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.out.println("Could not read the JSON file: " + e.getMessage());
            return 0;
        }
        if (content.trim().isEmpty()) {
            return 0;
        }

        int loaded = 0;
        for (String object : content.split("(?<=\\})\\s*,")) {
            if (!object.contains("\"messageID\"")) {
                continue;
            }
            String id        = extractJsonValue(object, "messageID");
            String hash      = extractJsonValue(object, "messageHash");
            String sender    = extractJsonValue(object, "sender");
            String recipient = extractJsonValue(object, "recipient");
            String text      = extractJsonValue(object, "message");

            Message m = new Message(id, loaded, sender, recipient, text, hash);
            storedMessages.add(m);
            messageIDs.add(id);
            messageHashes.add(m.messageHash);
            loaded++;
        }
        return loaded;
    }

    private static String extractJsonValue(String object, String key) {
        Matcher matcher = Pattern.compile("\"" + key + "\":\\s*\"((?:\\\\.|[^\"])*)\"").matcher(object);
        return matcher.find() ? unescapeJson(matcher.group(1)) : "";
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

    private static String unescapeJson(String text) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\\' && i + 1 < text.length()) {
                char next = text.charAt(++i);
                switch (next) {
                    case '"':  sb.append('"');  break;
                    case '\\': sb.append('\\'); break;
                    case 'n':  sb.append('\n'); break;
                    case 'r':  sb.append('\r'); break;
                    case 't':  sb.append('\t'); break;
                    default:   sb.append(next);
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    // ---------- Getters / test helpers ----------

    public String getMessageID()   { return messageID; }
    public String getMessageHash() { return messageHash; }
    public String getSender()      { return sender; }
    public String getRecipient()   { return recipient; }
    public String getMessageText() { return messageText; }

    public static String getStoragePath() { return storagePath; }
    public static void setStoragePath(String path) { storagePath = path; }

    public static void resetAll() {
        sentMessages.clear();
        disregardedMessages.clear();
        storedMessages.clear();
        messageHashes.clear();
        messageIDs.clear();
    }
}
