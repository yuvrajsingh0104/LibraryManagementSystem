import java.sql.*;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\nLibrary Management System");
            System.out.println("1. Add Book");
            System.out.println("2. Add Member");
            System.out.println("3. Issue Book");
            System.out.println("4. Return Book");
            System.out.println("5. Exit");
            System.out.print("Choose an option: ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1:
                    addBook(scanner);
                    break;
                case 2:
                    addMember(scanner);
                    break;
                case 3:
                    issueBook(scanner);
                    break;
                case 4:
                    returnBook(scanner);
                    break;
                case 5:
                    System.out.println("Exiting...");
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void addBook(Scanner scanner) {
        System.out.print("Enter book title: ");
        String title = scanner.nextLine();
        System.out.print("Enter book author: ");
        String author = scanner.nextLine();

        try (Connection connection = DBConnection.getConnection()) {
            String query = "INSERT INTO books (title, author, is_available) VALUES (?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, title);
            statement.setString(2, author);
            statement.setBoolean(3, true);

            int rowsInserted = statement.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("Book added successfully!");
            } else {
                System.out.println("Failed to add book.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void addMember(Scanner scanner) {
        System.out.print("Enter member name: ");
        String name = scanner.nextLine();
        System.out.print("Enter member email: ");
        String email = scanner.nextLine();

        try (Connection connection = DBConnection.getConnection()) {
            String query = "INSERT INTO members (name, email) VALUES (?, ?)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, name);
            statement.setString(2, email);

            int rowsInserted = statement.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("Member added successfully!");
            } else {
                System.out.println("Failed to add member.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void issueBook(Scanner scanner) {
        System.out.print("Enter book ID: ");
        int bookId = scanner.nextInt();
        System.out.print("Enter member ID: ");
        int memberId = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        try (Connection connection = DBConnection.getConnection()) {
            // Check if book is available
            String checkAvailabilityQuery = "SELECT is_available FROM books WHERE id = ?";
            PreparedStatement checkStatement = connection.prepareStatement(checkAvailabilityQuery);
            checkStatement.setInt(1, bookId);
            ResultSet resultSet = checkStatement.executeQuery();

            if (resultSet.next() && resultSet.getBoolean("is_available")) {
                // Issue the book
                String issueBookQuery = "INSERT INTO transactions (book_id, member_id, issue_date) VALUES (?, ?, NOW())";
                PreparedStatement issueStatement = connection.prepareStatement(issueBookQuery);
                issueStatement.setInt(1, bookId);
                issueStatement.setInt(2, memberId);

                int rowsInserted = issueStatement.executeUpdate();
                if (rowsInserted > 0) {
                    // Update book availability
                    String updateBookQuery = "UPDATE books SET is_available = FALSE WHERE id = ?";
                    PreparedStatement updateStatement = connection.prepareStatement(updateBookQuery);
                    updateStatement.setInt(1, bookId);
                    updateStatement.executeUpdate();

                    System.out.println("Book issued successfully!");
                } else {
                    System.out.println("Failed to issue book.");
                }
            } else {
                System.out.println("Book is not available.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void returnBook(Scanner scanner) {
        System.out.print("Enter transaction ID: ");
        int transactionId = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        try (Connection connection = DBConnection.getConnection()) {
            // Get the book ID from the transaction
            String getTransactionQuery = "SELECT book_id FROM transactions WHERE id = ?";
            PreparedStatement getTransactionStatement = connection.prepareStatement(getTransactionQuery);
            getTransactionStatement.setInt(1, transactionId);
            ResultSet resultSet = getTransactionStatement.executeQuery();

            if (resultSet.next()) {
                int bookId = resultSet.getInt("book_id");

                // Update the return date
                String returnBookQuery = "UPDATE transactions SET return_date = NOW() WHERE id = ?";
                PreparedStatement returnBookStatement = connection.prepareStatement(returnBookQuery);
                returnBookStatement.setInt(1, transactionId);
                int rowsUpdated = returnBookStatement.executeUpdate();

                if (rowsUpdated > 0) {
                    // Update book availability
                    String updateBookQuery = "UPDATE books SET is_available = TRUE WHERE id = ?";
                    PreparedStatement updateStatement = connection.prepareStatement(updateBookQuery);
                    updateStatement.setInt(1, bookId);
                    updateStatement.executeUpdate();

                    System.out.println("Book returned successfully!");
                } else {
                    System.out.println("Failed to return book.");
                }
            } else {
                System.out.println("Transaction not found.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
