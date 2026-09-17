import java.util.Scanner;

public class ChatApp {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== Welcome to the Chat App Registration ===");

        System.out.print("Enter your first name: ");
        String firstName = scanner.nextLine();

        System.out.print("Enter your last name: ");
        String lastName = scanner.nextLine();

        Login login = new Login(firstName, lastName);

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

        // Everything passed individually - now officially register the user.
        String registrationMessage = login.registerUser(username, password, cellPhoneNumber);
        System.out.println("\n" + registrationMessage);

        // ---------- LOGIN ----------
        System.out.println("\n=== Please log in ===");
        System.out.print("Enter your username: ");
        String loginUsername = scanner.nextLine();

        System.out.print("Enter your password: ");
        String loginPassword = scanner.nextLine();

        boolean loginSuccessful = login.loginUser(loginUsername, loginPassword);
        String loginStatusMessage = login.returnLoginStatus(loginSuccessful);
        System.out.println("\n" + loginStatusMessage);

        scanner.close();
    }
}
