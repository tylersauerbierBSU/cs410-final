import java.sql.*;
import java.util.Scanner;

public class GradeManagerShell {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/finalproject";
    private static final String USER = "user";
    private static final String PASS = "password";

    private Connection conn = null;
    private String currentClassId = null;

    public static void main(String[] args) {
        GradeManagerShell shell = new GradeManagerShell();
        shell.run();
    }

    public void run() {
        try {
            connectToDatabase();
            Scanner scanner = new Scanner(System.in);
            String command;

            System.out.println("Welcome to the Grade Management Shell. Type 'help' for commands.");
            while (true) {
                System.out.print("Command> ");
                command = scanner.nextLine();
                if (command.equals("exit")) {
                    break;
                }
                interpretCommand(command);
            }
            scanner.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeConnection();
        }
    }

    private void connectToDatabase() throws SQLException {
        conn = DriverManager.getConnection(DB_URL, USER, PASS);
        System.out.println("Connected to the database successfully.");
    }

    private void closeConnection() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
                System.out.println("Connection closed.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void interpretCommand(String command) {
        try {
            if (command.startsWith("new-class")) {
                createClass(command);
            } else if (command.equals("list-classes")) {
                listClasses();
            } else if (command.startsWith("select-class")) {
                selectClass(command);
            } else if (command.equals("show-class")) {
                showClass();
            } else {
                System.out.println("Unknown command. Type 'help' for commands.");
            }
        } catch (SQLException e) {
            System.out.println("Error executing command: " + e.getMessage());
        }
    }

    private void createClass(String command) throws SQLException {
        // Parse command to extract class details
        String[] parts = command.split(" ");
        if (parts.length < 5) {
            System.out.println("Invalid command syntax. Correct syntax: new-class <CourseNumber> <Term> <SectionNumber> \"<Description>\"");
            return;
        }
        String courseNumber = parts[1];
        String term = parts[2];
        int sectionNumber = Integer.parseInt(parts[3]);
        String description = command.substring(command.indexOf('"') + 1, command.lastIndexOf('"'));

        String sql = "INSERT INTO Courses (CourseNumber, Term, SectionNumber, Description) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, courseNumber);
            stmt.setString(2, term);
            stmt.setInt(3, sectionNumber);
            stmt.setString(4, description);
            stmt.executeUpdate();
            System.out.println("Class created successfully.");
        }
    }

    private void listClasses() throws SQLException {
        String sql = "SELECT CourseID, CourseNumber, Term, SectionNumber, Description FROM Courses";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                System.out.println(rs.getString("CourseNumber") + " " + rs.getString("Term") + " Section " + rs.getInt("SectionNumber") + ": " + rs.getString("Description"));
            }
        }
    }

    private void selectClass(String command) throws SQLException {
        String[] parts = command.split(" ");
        String sql;
        PreparedStatement stmt;

        switch (parts.length) {
            case 2:
                sql = "SELECT CourseID FROM Courses WHERE CourseNumber = ? ORDER BY Term DESC, SectionNumber ASC LIMIT 1";
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, parts[1]);
                break;
            case 3:
                sql = "SELECT CourseID FROM Courses WHERE CourseNumber = ? AND Term = ? ORDER BY SectionNumber ASC LIMIT 1";
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, parts[1]);
                stmt.setString(2, parts[2]);
                break;
            case 4:
                sql = "SELECT CourseID FROM Courses WHERE CourseNumber = ? AND Term = ? AND SectionNumber = ?";
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, parts[1]);
                stmt.setString(2, parts[2]);
                stmt.setInt(3, Integer.parseInt(parts[3]));
                break;
            default:
                System.out.println("Invalid select-class command format.");
                return;
        }

        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                currentClassId = rs.getString("CourseID");
                System.out.println("Class selected: ID " + currentClassId);
            } else {
                System.out.println("Class not found or multiple sections found.");
            }
        }
    }

    private void showClass() throws SQLException {
        if (currentClassId == null) {
            System.out.println("No class is currently selected.");
            return;
        }

        String sql = "SELECT CourseNumber, Term, SectionNumber, Description FROM Courses WHERE CourseID = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, Integer.parseInt(currentClassId));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("Current Class: " + rs.getString("CourseNumber") + " " + rs.getString("Term") + " Section " + rs.getInt("SectionNumber") + ": " + rs.getString("Description"));
                } else {
                    System.out.println("Class with ID " + currentClassId + " does not exist.");
                }
            }
        }
    }
}
