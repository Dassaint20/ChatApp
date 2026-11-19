import java.util.Scanner;

public class ChatApp {

    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {

        System.out.println("=== Welcome to the Chat App Registration ===");

        System.out.print("Enter your first name: ");
        String firstName = scanner.nextLine();

        System.out.print("Enter your last name: ");
        String lastName = scanner.nextLine();

        Login login = new Login(firstName, lastName);

        // ---------- USERNAME ----------
        String username;
        boolean isUsernameValid;
        do {
            System.out.print("\nEnter a username (must contain an underscore and be no more than 5 characters): ");
            username = scanner.nextLine();
            isUsernameValid = login.checkUserName(username);

            if (isUsernameValid) {
                System.out.println("Username successfully captured.");
            } else {
                System.out.println("Username is not correctly formatted; please ensure that your "
                        + "username contains an underscore and is no more than five characters in length.");
            }
        } while (!isUsernameValid);

        // ---------- PASSWORD ----------
        String password;
        boolean isPasswordValid;
        do {
            System.out.print("\nEnter a password (min 8 characters, 1 capital letter, 1 number, 1 special character): ");
            password = scanner.nextLine();
            isPasswordValid = login.checkPasswordComplexity(password);

            if (isPasswordValid) {
                System.out.println("Password successfully captured.");
            } else {
                System.out.println("Password is not correctly formatted; please ensure that the password "
                        + "contains at least eight characters, a capital letter, a number, and a special character.");
            }
        } while (!isPasswordValid);

        // ---------- CELL PHONE NUMBER ----------
        String cellPhoneNumber;
        boolean isCellValid;
        do {
            System.out.print("\nEnter your South African cell phone number (e.g. +27838968976): ");
            cellPhoneNumber = scanner.nextLine();
            isCellValid = login.checkCellPhoneNumber(cellPhoneNumber);

            if (isCellValid) {
                System.out.println("Cell phone number successfully added.");
            } else {
                System.out.println("Cell phone number incorrectly formatted or does not contain international code.");
            }
        } while (!isCellValid);

        String registrationMessage = login.registerUser(username, password, cellPhoneNumber);
        System.out.println("\n" + registrationMessage);

        // ---------- LOGIN ----------
        System.out.println("\n=== Please log in ===");
        System.out.print("Enter your username: ");
        String loginUsername = scanner.nextLine();

        System.out.print("Enter your password: ");
        String loginPassword = scanner.nextLine();

        boolean loginSuccessful = login.loginUser(loginUsername, loginPassword);
        System.out.println("\n" + login.returnLoginStatus(loginSuccessful));

        // ---------- PART 2 & 3: only logged-in users may send messages ----------
        if (loginSuccessful) {
            runQuickChat(login.getCellPhoneNumber());
        }

        scanner.close();
    }

    // =====================================================================
    // PART 2 - QuickChat
    // =====================================================================

    private static void runQuickChat(String sender) {
        System.out.println("\nWelcome to QuickChat.");

        int maxMessages = readInt("\nHow many messages do you wish to enter? ", 1, Integer.MAX_VALUE);
        int messagesEntered = 0;   // every message entered counts against the limit (sent, discarded or stored)

        boolean running = true;
        while (running) {
            System.out.println("\n--- QuickChat Menu ---");
            System.out.println("1) Send Messages");
            System.out.println("2) Show recently sent messages");
            System.out.println("3) Quit");
            System.out.println("4) Stored Messages");

            int choice = readInt("Choose an option: ", 1, 4);

            switch (choice) {
                case 1:
                    if (messagesEntered >= maxMessages) {
                        System.out.println("You have already entered the " + maxMessages + " message(s) you set.");
                        break;
                    }
                    for (; messagesEntered < maxMessages; messagesEntered++) {
                        System.out.println("\n--- Message " + (messagesEntered + 1) + " of " + maxMessages + " ---");
                        enterMessage(messagesEntered, sender);
                    }
                    System.out.println("\nTotal number of messages sent: "
                            + Message.returnTotalMessages());
                    break;
                case 2:
                    // Full report of every message sent: Message Hash, Recipient, Message.
                    System.out.println("\n" + Message.printMessages());
                    break;
                case 3:
                    System.out.println("\nTotal number of messages sent: "
                            + Message.returnTotalMessages());
                    System.out.println("Goodbye!");
                    running = false;
                    break;
                case 4:
                    // Read the JSON file back into the Stored Messages array, then show the submenu.
                    int loaded = Message.loadStoredMessages();
                    System.out.println("\nLoaded " + loaded + " stored message(s) from "
                            + Message.getStoragePath());
                    storedMessagesMenu();
                    break;
                default:
                    break;
            }
        }
    }

    // =====================================================================
    // PART 3 - Stored Messages submenu (4th main menu option)
    // =====================================================================

    private static void storedMessagesMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Stored Messages Menu ---");
            System.out.println("a) Display the sender and recipient of all stored messages");
            System.out.println("b) Display the longest stored message");
            System.out.println("c) Search for a message by message ID");
            System.out.println("d) Search for all messages sent/stored for a particular recipient");
            System.out.println("e) Delete a message using the message hash");
            System.out.println("f) Display a report of all stored messages");
            System.out.println("g) Back to main menu");

            System.out.print("Choose an option: ");
            String option = scanner.nextLine().trim().toLowerCase();

            switch (option) {
                case "a":
                    System.out.println("\n" + Message.displaySenderAndRecipient());
                    break;
                case "b":
                    System.out.println("\nLongest stored message:\n" + Message.displayLongestStoredMessage());
                    break;
                case "c":
                    System.out.print("\nEnter the message ID to search for: ");
                    System.out.println(Message.searchByMessageId(scanner.nextLine().trim()));
                    break;
                case "d":
                    System.out.print("\nEnter the recipient's cell number (with international code): ");
                    System.out.println(Message.searchByRecipient(scanner.nextLine().trim()));
                    break;
                case "e":
                    System.out.print("\nEnter the message hash of the message to delete: ");
                    System.out.println(Message.deleteMessageByHash(scanner.nextLine().trim()));
                    break;
                case "f":
                    System.out.println("\n" + Message.displayReport());
                    break;
                case "g":
                    back = true;
                    break;
                default:
                    System.out.println("Invalid option. Please choose a letter from a to g.");
                    break;
            }
        }
    }

    /** Captures one message, then lets the user send, disregard or store it. */
    private static void enterMessage(int messageNumber, String sender) {
        // --- Recipient ---
        String recipient;
        String recipientStatus;
        do {
            System.out.print("Enter the recipient's cell number (with international code, e.g. +27718693002): ");
            recipient = scanner.nextLine().trim();
            recipientStatus = new Message("0000000000", messageNumber, recipient, "").checkRecipientCell();
            System.out.println(recipientStatus);
        } while (!recipientStatus.equals("Cell phone number successfully captured."));

        // --- Message text ---
        String text;
        String lengthStatus;
        do {
            System.out.print("Enter your message (max 250 characters): ");
            text = scanner.nextLine();
            lengthStatus = new Message("0000000000", messageNumber, recipient, text).checkMessageLength();
            if (!lengthStatus.equals("Message ready to send.")) {
                System.out.println("Please enter a message of less than 250 characters.");
            }
            System.out.println(lengthStatus);
        } while (!lengthStatus.equals("Message ready to send."));

        // --- Build the message (ID is auto generated) ---
        Message message = new Message(Message.generateMessageID(), messageNumber, sender, recipient, text);
        System.out.println(message.getMessageIDStatus());

        // --- Send / disregard / store ---
        System.out.println("\nWhat would you like to do with this message?");
        System.out.println("1) Send Message");
        System.out.println("2) Disregard Message");
        System.out.println("3) Store Message to send later");
        int action = readInt("Choose an option: ", 1, 3);

        System.out.println(message.sentMessage(action));

        if (action == 1) {
            System.out.println("\n" + message.displayDetails());
        } else if (action == 2) {
            int zero;
            do {
                zero = readInt("Enter 0 to delete the message: ", 0, 0);
            } while (zero != 0);
            System.out.println("Message deleted.");
        }
    }

    /** Keeps asking until the user types a whole number between min and max. */
    private static int readInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                int value = Integer.parseInt(input);
                if (value >= min && value <= max) {
                    return value;
                }
            } catch (NumberFormatException e) {
                // fall through to the error message
            }
            if (max == Integer.MAX_VALUE) {
                System.out.println("Please enter a whole number of " + min + " or more.");
            } else {
                System.out.println("Please enter a number between " + min + " and " + max + ".");
            }
        }
    }
}

