//STEP 1. Import required packages
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Demo {

    // Set JDBC driver name and database URL
    static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://localhost:3306/buscanceldb";

    // Database credentials
    static final String USER = "root"; // Add your MySQL username
    static final String PASSWORD = "Maahi@123"; // Add your MySQL password

    // Main function
    public static void main(String[] args) {
        Connection conn = null;
        Scanner scanner = new Scanner(System.in);

        // Connecting to the Database
        try {
            // STEP 2a: Register JDBC driver
            Class.forName(JDBC_DRIVER);
            // STEP 2b: Open a connection
            System.out.println("Connecting to database...");
            conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);

            // Disable auto-commit for transaction management
            conn.setAutoCommit(false);

            // Display menu
            while (true) {
                System.out.println("\n--- Menu ---");
                System.out.println("1. Cancel Bus Ticket");
                System.out.println("2. Notify Passengers of Bus Cancellation");
                System.out.println("3. Process Refunds for Cancelled Buses");
                System.out.println("4. Generate Bus Cancellation Report");
                System.out.println("5. Exit");
                System.out.print("Enter choice: ");
                int choice = scanner.nextInt();
                scanner.nextLine();

                // Execute use cases
                switch (choice) {
                    case 1:
                        cancelBusTicket(conn, scanner);
                        break;
                    case 2:
                        notifyPassengers(conn);
                        break;
                    case 3:
                        processRefunds(conn);
                        break;
                    case 4:
                        generateCancellationReport(conn);
                        break;
                    case 5:
                        System.out.println("Exiting...");
                        return;
                    default:
                        System.out.println("Invalid choice.");
                }
            }
        } catch (SQLException se) { // Handle errors for JDBC
            try {
                if (conn != null) {
                    conn.rollback();
                    System.out.println("Failed and rolled back!");
                }
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            se.printStackTrace();
        } catch (Exception e) { // Handle errors for Class.forName
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }

        scanner.close();
        System.out.println("End of Code");
    } // End main

    private static double calculateRefundAmount(String cancellationReason) {
        double refundAmount = 0.0;
        switch (cancellationReason) {
            case "Health Issues":
                refundAmount = 100.0;
                break;
            case "Personal Emergency":
                refundAmount = 75.0;
                break;
            case "Plan Change":
                refundAmount = 50.0;
                break;
            case "Other":
                refundAmount = 0.0;  // No refund for "Other"
                break;
            default:
                break;
        }
        return refundAmount;
    }
    

   // Use case function to cancel a bus ticket
   static void cancelBusTicket(Connection conn, Scanner scanner) {
    System.out.println("Enter the following details: ");
    try {
        System.out.print("Enter Booking ID: ");
        int bookingId = scanner.nextInt();
        scanner.nextLine();
        
        String checkBookingQuery = "SELECT Status FROM Bookings WHERE Booking_ID = ?";
        PreparedStatement checkStmt = conn.prepareStatement(checkBookingQuery);
        checkStmt.setInt(1, bookingId);
        ResultSet rs = checkStmt.executeQuery();
        
        if (!rs.next()) {
            System.out.println("No booking found with ID " + bookingId);
            return;
        } else {
            String bookingStatus = rs.getString("Status");
            if ("Cancelled".equalsIgnoreCase(bookingStatus)) {
                System.out.println("This booking has already been cancelled.");
                return;
            }
        }

        String cancellationReason = "";
        boolean validChoice = false;
        
        while (!validChoice) {
            System.out.println("Please select a cancellation reason from the following options:");
            System.out.println("1. Health Issues");
            System.out.println("2. Personal Emergency");
            System.out.println("3. Plan Change");
            System.out.println("4. Other");
            System.out.print("Enter your choice (1-4): ");
            try {
                int reasonChoice = scanner.nextInt();
                scanner.nextLine();
                switch (reasonChoice) {
                    case 1 -> { cancellationReason = "Health Issues"; validChoice = true; }
                    case 2 -> { cancellationReason = "Personal Emergency"; validChoice = true; }
                    case 3 -> { cancellationReason = "Plan Change"; validChoice = true; }
                    case 4 -> { cancellationReason = "Other"; validChoice = true; }
                    default -> System.out.println("Invalid choice, please select a number between 1 and 4.");
                }
            } catch (InputMismatchException e) {
                scanner.nextLine();
                System.out.println("Invalid input. Please enter a valid number (1-4).");
            }
        }

        String cancelQuery = "UPDATE Bookings SET Status = 'Cancelled' WHERE Booking_ID = ?";
        PreparedStatement cancelStmt = conn.prepareStatement(cancelQuery);
        cancelStmt.setInt(1, bookingId);
        int rowsUpdated = cancelStmt.executeUpdate();
        
        if (rowsUpdated > 0) {
            System.out.println("Booking ID " + bookingId + " has been cancelled successfully.");
            conn.setAutoCommit(false);
            
            String getMaxCancellationIdQuery = "SELECT MAX(Cancellation_ID) AS max_id FROM Cancellations FOR UPDATE";
            PreparedStatement maxStmt = conn.prepareStatement(getMaxCancellationIdQuery);
            ResultSet maxRs = maxStmt.executeQuery();
            int cancellationId = 1;
            if (maxRs.next()) {
                cancellationId = maxRs.getInt("max_id") + 1;
            }
            
            String insertCancellationQuery = "INSERT INTO Cancellations (Cancellation_ID, Booking_ID, Cancellation_Reason) VALUES (?, ?, ?)";
            PreparedStatement insertCancelStmt = conn.prepareStatement(insertCancellationQuery);
            insertCancelStmt.setInt(1, cancellationId);
            insertCancelStmt.setInt(2, bookingId);
            insertCancelStmt.setString(3, cancellationReason);
            insertCancelStmt.executeUpdate();
            
            conn.commit();
            System.out.println("Cancellation recorded successfully with ID: " + cancellationId);
        }
    } catch (SQLException e) {
        try {
            conn.rollback();
            System.out.println("Cancellation failed. Rolled back.");
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        e.printStackTrace();
    }
}


    

static void notifyPassengers(Connection conn) {
    String notificationQuery = "SELECT b.Booking_ID, b.Bus_ID, p.Name, c.Cancellation_Reason " +
                                "FROM Bookings b " +
                                "JOIN Cancellations c ON b.Booking_ID = c.Booking_ID " +
                                "JOIN Passengers p ON b.Passenger_ID = p.Passenger_ID " +
                                "WHERE b.Status = 'Cancelled' AND NOT EXISTS " +
                                "(SELECT 1 FROM Notifications n WHERE n.Booking_ID = b.Booking_ID)";

    try (Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(notificationQuery)) {

        boolean hasData = false;
        while (rs.next()) {
            hasData = true;
            int bookingId = rs.getInt("Booking_ID");
            String passengerName = rs.getString("Name");
            String busName = getBusName(conn, rs.getInt("Bus_ID"));
            String cancellationReason = rs.getString("Cancellation_Reason");

            // Ask for contact info first
            Scanner scanner = new Scanner(System.in);
            System.out.println("Enter contact info for " + passengerName + ": ");
            String inputContactInfo = scanner.nextLine();

            // Validate the contact info
            String validateContactInfoQuery = "SELECT Contact_Info, Email FROM Passengers WHERE Name = ? AND Contact_Info = ?";
            try (PreparedStatement validateContactStmt = conn.prepareStatement(validateContactInfoQuery)) {
                validateContactStmt.setString(1, passengerName);
                validateContactStmt.setString(2, inputContactInfo);

                ResultSet validateContactRs = validateContactStmt.executeQuery();

                if (validateContactRs.next()) {
                    // Contact info is valid, now ask for email
                    String storedEmail = validateContactRs.getString("Email");
                    System.out.println("Enter email for " + passengerName + ": ");
                    String inputEmail = scanner.nextLine();

                    // Validate the email
                    if (storedEmail.equals(inputEmail)) {
                        // Email matches, send notification
                        String message = String.format("Hey %s, your booking with Booking ID: %d for %s has been cancelled due to: %s. We apologize for the inconvenience.",
                                                       passengerName, bookingId, busName, cancellationReason);

                        // Insert notification into the database
                        String insertNotificationQuery = "INSERT INTO Notifications (Booking_ID, Message) VALUES (?, ?)";
                        try (PreparedStatement notificationStmt = conn.prepareStatement(insertNotificationQuery)) {
                            notificationStmt.setInt(1, bookingId);
                            notificationStmt.setString(2, message);
                            notificationStmt.executeUpdate();
                        }

                        // Log the notification
                        System.out.println("Notification sent to " + passengerName + " for Booking ID: " + bookingId);
                    } else {
                        // Email does not match
                        System.out.println("Email does not match the contact info. Returning to main menu.");
                        return; // Return to the main menu after invalid email
                    }
                } else {
                    // Invalid contact info
                    System.out.println("Contact info does not match records. Returning to main menu.");
                    return; // Return to the main menu after invalid contact info
                }
            }
        }

        if (!hasData) {
            System.out.println("Sorry, no notifications to send.");
        }

        conn.commit();
    } catch (SQLException e) {
        e.printStackTrace();
    }
}


// Helper function to get bus name
private static String getBusName(Connection conn, int busId) throws SQLException {
    String query = "SELECT Bus_Name FROM Buses WHERE Bus_ID = ?";
    try (PreparedStatement stmt = conn.prepareStatement(query)) {
        stmt.setInt(1, busId);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            return rs.getString("Bus_Name");
        }
    }
    return "Unknown Bus";
}



static void processRefunds(Connection conn) {
    String refundQuery = "SELECT c.Cancellation_ID, b.Booking_ID, p.Name, c.Cancellation_Reason " +
                         "FROM Cancellations c " +
                         "JOIN Bookings b ON c.Booking_ID = b.Booking_ID " +
                         "JOIN Passengers p ON b.Passenger_ID = p.Passenger_ID " +
                         "WHERE c.Refund_Status = 'Pending' AND NOT EXISTS " +
                         "(SELECT 1 FROM Refunds r WHERE r.Cancellation_ID = c.Cancellation_ID)";

    try (Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(refundQuery)) {

        // Check if any data is returned
        boolean hasData = false;
        while (rs.next()) {
            hasData = true;
            int cancellationId = rs.getInt("Cancellation_ID");
            int bookingId = rs.getInt("Booking_ID");
            String passengerName = rs.getString("Name");
            String cancellationReason = rs.getString("Cancellation_Reason");

            // Calculate refund amount based on cancellation reason
            double refundAmount = calculateRefundAmount(cancellationReason);

            // Get current date and time for refund date
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String refundDate = sdf.format(new Date());

            // Insert refund record
            String insertRefundQuery = "INSERT INTO Refunds (Cancellation_ID, Refund_Amount, Refund_Date) VALUES (?, ?, ?)";
            try (PreparedStatement refundStmt = conn.prepareStatement(insertRefundQuery)) {
                refundStmt.setInt(1, cancellationId);
                refundStmt.setDouble(2, refundAmount);
                refundStmt.setString(3, refundDate); // Insert current timestamp
                refundStmt.executeUpdate();
            }

            // Update cancellation status
            String updateCancellationQuery = "UPDATE Cancellations SET Refund_Status = 'Processed' WHERE Cancellation_ID = ?";
            try (PreparedStatement updateStmt = conn.prepareStatement(updateCancellationQuery)) {
                updateStmt.setInt(1, cancellationId);
                updateStmt.executeUpdate();
            }

            // Log the refund processing
            System.out.println("Refund processed for Booking ID: " + bookingId + " (" + passengerName + "). Refund Amount: ₹" + refundAmount + " on " + refundDate);
        }

        if (!hasData) {
            System.out.println("Sorry, no refunds to process.");
        }

        // Handle already processed refunds
        String alreadyProcessedQuery = "SELECT c.Cancellation_ID, b.Booking_ID, p.Name " +
                                       "FROM Cancellations c " +
                                       "JOIN Bookings b ON c.Booking_ID = b.Booking_ID " +
                                       "JOIN Passengers p ON b.Passenger_ID = p.Passenger_ID " +
                                       "WHERE c.Refund_Status = 'Processed' AND EXISTS " +
                                       "(SELECT 1 FROM Refunds r WHERE r.Cancellation_ID = c.Cancellation_ID)";
        try (ResultSet rsAlreadyProcessed = stmt.executeQuery(alreadyProcessedQuery)) {
            while (rsAlreadyProcessed.next()) {
                int bookingId = rsAlreadyProcessed.getInt("Booking_ID");
                String passengerName = rsAlreadyProcessed.getString("Name");
                System.out.println("Refund already processed for Booking ID: " + bookingId + " (" + passengerName + ")");
            }
        }

        conn.commit();
    } catch (SQLException e) {
        e.printStackTrace();
    }
}





    // Helper function to get passenger name
    private static String getPassengerName(Connection conn, int bookingId) throws SQLException {
        String query = "SELECT p.Name FROM Bookings b JOIN Passengers p ON b.Passenger_ID = p.Passenger_ID WHERE b.Booking_ID = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, bookingId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("Name");
            }
        }
        return "Unknown Passenger";
    }
    
    public static void generateCancellationReport(Connection conn) {
        try {
            Statement stmt = conn.createStatement();
            
            // First, show summary
            ResultSet summary = stmt.executeQuery(
                "SELECT COUNT(*) AS total, IFNULL(SUM(r.Refund_Amount), 0) AS total_refunded " +
                "FROM Cancellations c " +
                "LEFT JOIN Refunds r ON c.Cancellation_ID = r.Cancellation_ID"
            );
            
            if (summary.next()) {
                int totalCancellations = summary.getInt("total");
                double totalRefunded = summary.getDouble("total_refunded");
                
                // If no cancellations or refunds exist, display a message
                if (totalCancellations == 0 && totalRefunded == 0) {
                    System.out.println("Sorry, no report can be generated as no cancellations or refunds have been made yet.");
                    return;  // Exit the function early
                }
    
                System.out.println("\nBus Cancellation Report:");
                System.out.println("Total Cancellations: " + totalCancellations);
                System.out.println("Total Refunded Amount: ₹" + totalRefunded);
            }
            
            // Now, show detailed report if there are cancellations
            System.out.println("\n Detailed Cancellation Report:");
            ResultSet rs = stmt.executeQuery(
                "SELECT p.Name, p.Contact_Info, p.Email, b.Booking_ID, c.Cancellation_Reason, r.Refund_Amount, r.Refund_Date " +
                "FROM Cancellations c " +
                "JOIN Bookings b ON c.Booking_ID = b.Booking_ID " +
                "JOIN Passengers p ON b.Passenger_ID = p.Passenger_ID " +
                "LEFT JOIN Refunds r ON c.Cancellation_ID = r.Cancellation_ID"
            );
    
            // If no detailed data is found, notify that no cancellations exist
            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                System.out.println("-----------------------------------");
                System.out.println("Passenger Name     : " + rs.getString("Name"));
                System.out.println("Contact Info       : " + rs.getString("Contact_Info"));
                System.out.println("Email              : " + rs.getString("Email"));
                System.out.println("Booking ID         : " + rs.getInt("Booking_ID"));
                System.out.println("Cancellation Reason: " + rs.getString("Cancellation_Reason"));
                System.out.println("Refund Amount      : ₹" + rs.getDouble("Refund_Amount"));
                System.out.println("Refund Date        : " + rs.getTimestamp("Refund_Date"));
                System.out.println("-----------------------------------\n");
            }
    
            if (!hasData) {
                System.out.println("Sorry, no cancellations found in the system.");
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
} // End class   
