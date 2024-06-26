/*
 * GradeManagerShell - A simple command-line interface for managing grades and students in a database.
 * Author: Anton Leslie, Andrew Martinez, Tyler Sauerbier
 * Description: This program provides a shell interface to interact with a database containing information about classes, assignments, categories, and students.
 */

 import java.sql.*;
 import java.util.Scanner;
 
 public class GradeManagerShell {
     // Database connection parameters
     private static final String DB_URL = "jdbc:mysql://localhost:3306/finalproject";
     private static final String USER = "user";
     private static final String PASS = "password";
 
     // Database connection object
     private Connection conn = null;
     // Variable to store the current class ID
     private String currentClassId = null;
 
     // Main method
     public static void main(String[] args) {
         GradeManagerShell shell = new GradeManagerShell();
         shell.run();
     }
 
     // Method to run the shell
     public void run() {
         try {
             connectToDatabase(); // Connect to the database
             Scanner scanner = new Scanner(System.in);
             String command;
 
             // Display welcome message
             System.out.println("Welcome to the Grade Management Shell. Type 'help' for commands.");
 
             // Main command loop
             while (true) {
                 System.out.print("Command> ");
                 command = scanner.nextLine();
                 if (command.equals("exit")) {
                     break;
                 }
                 interpretCommand(command); // Interpret and execute the command
             }
             scanner.close();
         } catch (SQLException e) {
             e.printStackTrace();
         } finally {
             closeConnection(); // Close the database connection
         }
     }
 
     // Method to connect to the database
     private void connectToDatabase() throws SQLException {
         conn = DriverManager.getConnection(DB_URL, USER, PASS);
         System.out.println("Connected to the database successfully.");
     }
 
     // Method to close the database connection
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
 
     // Method to interpret and execute the user command
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
             } else if (command.equals("show-categories")) {
                 showCategories();
             } else if (command.startsWith("add-category")) {
                 addCategory(command);
             } else if (command.equals("show-assignment")) {
                 showAssignments();
             } else if (command.startsWith("add-assignment")) {
                 addAssignment(command);
             } else if (command.startsWith("add-student")) {
                 addStudent(command);
             } else if (command.equals("show-students")) {
                 showStudents();
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

    // Methods for the Category and Assignment Management

    /**
     * Method to list all the categories with their weights
     */
    private void showCategories() throws SQLException {
        if (currentClassId == null) {
            System.out.println("No class is currently selected.");
            return;
        }

        String sql = "SELECT Name, Weight FROM Categories WHERE CourseID = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, Integer.parseInt(currentClassId)); // Assuming currentClassId is a String

        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                System.out.println("Categories for Course ID " + currentClassId + ":");
                do {
                    String name = rs.getString("Name");
                    double weight = rs.getDouble("Weight");
                    System.out.printf("  - %s (%.2f%%)\n", name, weight);
                } while (rs.next());
            } else {
                System.out.println("No categories found for this class.");
            }
        }
    }


    /**
     * Method to add a new category
     * @param - command
     */
    private void addCategory(String command) throws SQLException {
        if (currentClassId == null) {
            System.out.println("No class is currently selected.");
            return;
        }

        // Extract category name and weight from the command
        String[] parts = command.split(" ");
        if (parts.length != 3) {
            System.out.println("Invalid add-category command format. Usage: add-category <name> <weight>");
            return;
        }

        String name = parts[1];
        double weight;
        try {
            weight = Double.parseDouble(parts[2]);
        } catch (NumberFormatException e) {
            System.out.println("Invalid weight. Please enter a decimal number.");
            return;
        }

        // Validate weight (0 to 100)
        if (weight < 0 || weight > 100) {
            System.out.println("Weight must be between 0 and 100.");
            return;
        }

        // Prepare and execute the insert statement
        String sql = "INSERT INTO Categories (CourseID, Name, Weight) VALUES (?, ?, ?)";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, Integer.parseInt(currentClassId));
        stmt.setString(2, name);
        stmt.setDouble(3, weight);

        try {
            stmt.executeUpdate();
            System.out.println("Category '" + name + "' added successfully.");
        } catch (SQLException e) {
            System.out.println("Error adding category: " + e.getMessage());
        }
    }


    /**
     * Lists all the assignments with their point values grouped by category
     */
    private void showAssignments() throws SQLException {
        if (currentClassId == null) {
            System.out.println("No class is currently selected.");
            return;
        }

        String sql = "SELECT c.Name AS CategoryName, a.Name AS AssignmentName, a.Points " +
                "FROM Assignments a " +
                "INNER JOIN Categories c ON a.CategoryID = c.CategoryID " +
                "WHERE c.CourseID = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, Integer.parseInt(currentClassId));

        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                System.out.println("Assignments for Course ID " + currentClassId + ":");
                String currentCategory = null;
                do {
                    String categoryName = rs.getString("CategoryName");
                    String assignmentName = rs.getString("AssignmentName");
                    int points = rs.getInt("Points");

                    if (currentCategory == null || !currentCategory.equals(categoryName)) {
                        // New category
                        if (currentCategory != null) {
                            System.out.println("  - Total: - points"); // Add total for previous category (if any)
                        }
                        currentCategory = categoryName;
                        System.out.println("  Category: " + categoryName);
                    }

                    System.out.printf("    - %s (%d points)\n", assignmentName, points);
                } while (rs.next());

                // Print total for the last category
                if (currentCategory != null) {
                    System.out.println("  - Total: - points"); // Placeholder, calculate actual total here
                }
            } else {
                System.out.println("No assignments found for this class.");
            }
        }
    }

    /**
     * Adds a new assignment
     */
    private void addAssignment(String command) throws SQLException {
        if (currentClassId == null) {
            System.out.println("No class is currently selected.");
            return;
        }

        String[] parts = command.split(" ");

        if (parts.length != 5) {
            System.out.println("Invalid add-assignment command format. Usage: add-assignment <name> <category> <description> <points>");
            return;
        }

        String name = parts[1];
        String category = parts[2];
        String description = parts[3];
        int points;
        try {
            points = Integer.parseInt(parts[4]);
        } catch (NumberFormatException e) {
            System.out.println("Invalid points format. Points must be an integer.");
            return;
        }

        String sql = "INSERT INTO Assignments (CourseID, Name, Category, Description, Points) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, Integer.parseInt(currentClassId));
            stmt.setString(2, name);
            stmt.setString(3, category);
            stmt.setString(4, description);
            stmt.setInt(5, points);

            stmt.executeUpdate();
            System.out.println("Assignment '" + name + "' added successfully.");
        } catch (SQLException e) {
            System.out.println("Error adding assignment: " + e.getMessage());
        }
    }


    // STUDENT MANAGEMENT CODE

    /**
     * Adds a new student
     */
    private void addStudent(String command) throws SQLException {
        if (currentClassId == null) {
            System.out.println("No class is currently selected.");
            return;
        }

        String[] parse = command.split(" ");

        if (parse.length != 2 && parse.length != 5) {
            System.out.println("Invalid add-assignment command format. Usage: add-assignment <username> " +
                    "[<studentID> <Last> <First>]");
            return;
        }

        String sql;

        if (parse.length == 2) { // this means it's the already existing student command
            sql = "INSERT INTO Enrollments (StudentID, CourseID) " +
                    "SELECT StudentID, ? " +
                    "FROM Students " +
                    "WHERE Username = ? ";

            PreparedStatement enrollStatement = conn.prepareStatement(sql);
            enrollStatement.setInt(1, Integer.parseInt(currentClassId));
            enrollStatement.setString(2, parse[1]);

            try {
                enrollStatement.executeUpdate();
                System.out.println("Student enrolled successfully");
            } catch (SQLException e) {
                System.out.println("Error adding student or enrolling: " + e.getMessage());
            }
        }
        else { // this means it's the new student

            // first insert into students table
            sql = "INSERT INTO Students (StudentID, Username, Name) " +
                    "VALUES (?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE Name = VALUES(Name) ";

            PreparedStatement studentStmt = conn.prepareStatement(sql);
            studentStmt.setInt(1, Integer.parseInt(parse[2]));
            studentStmt.setString(2, parse[1]);
            studentStmt.setString(3, parse[3] + " " + parse[4]);

            sql = "INSERT INTO Enrollments (StudentID, CourseID) " +
                    "SELECT StudentID, ? " +
                    "FROM Students " +
                    "WHERE Username = ? ";

            PreparedStatement enrollStatement = conn.prepareStatement(sql);
            enrollStatement.setInt(1, Integer.parseInt(currentClassId));
            enrollStatement.setString(2, parse[1]);

            try {
                int r = studentStmt.executeUpdate();
                enrollStatement.executeUpdate();
                if (r == 1) {
                    System.out.println("Student added and enrolled successfully");
                }
                else {
                    System.out.println("Student name updated and enrolled successfully");
                }
            } catch (SQLException e) {
                System.out.println("Error adding student or enrolling: " + e.getMessage());
            }
        }
    }

    private void showStudents() throws SQLException {
        if (currentClassId == null) {
            System.out.println("No class is currently selected.");
            return;
        }

        String sql = "SELECT s.Name " +
                "FROM Students s " +
                "INNER JOIN Enrollments e ON s.StudentID = e.StudentID " +
                "WHERE e.CourseID = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, Integer.parseInt(currentClassId));

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("No students enrolled in this class.");
                } else {
                    System.out.println("Students:");
                    do {
                        String name = rs.getString("Name");
                        System.out.println(name);
                    } while (rs.next());
                }
            }
        } catch (SQLException e) {
            System.out.println("Error fetching students: " + e.getMessage());
        }
    }

}
