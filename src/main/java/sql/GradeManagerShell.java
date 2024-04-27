package sql;

import java.sql.*;
import java.util.Scanner;

public class GradeManagerShell {
    private Connection conn = null;
    private String currentClassId = null;
    private static int port;
    private static String password;

    public static void main(String[] args) {
        port = Integer.parseInt(args[0]);
        password = args[1];
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
        conn = DatabaseSetup.getDatabaseConnection(port, password);
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
            } else if (command.startsWith("show-students")) {
                showStudents(command);
            }  else if (command.equals("gradebook")) {
                showGradebook();
            } else if (command.startsWith("grade")) {
                assignGrade(command);
            } else if (command.startsWith("student-grades")) {
                showStudentGrades(command);
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
        String description = parts[4];

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
        String sql = "SELECT c.CourseNumber, c.Term, c.SectionNumber, c.Description, COUNT(e.StudentID) AS EnrolledStudents " +
                "FROM Courses c " +
                "LEFT JOIN Enrollments e ON c.CourseID = e.CourseID " +
                "GROUP BY c.CourseID, c.CourseNumber, c.Term, c.SectionNumber, c.Description " +
                "ORDER BY c.CourseNumber, c.Term, c.SectionNumber";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                System.out.println(rs.getString("CourseNumber") + " " + rs.getString("Term") + " Section " + rs.getInt("SectionNumber") + ": " + rs.getString("Description") + " (" + rs.getInt("EnrolledStudents") + " students)");
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
                            System.out.println(); // Add an empty line between categories
                        }
                        currentCategory = categoryName;
                        System.out.println("  Category: " + categoryName);
                    }

                    System.out.printf("    - %s (%d points)\n", assignmentName, points);
                } while (rs.next());
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

        // Check for existing assignment with the same name within the category for the current course
        String sqlCheck = "SELECT 1 FROM Assignments a " +
                "INNER JOIN Categories c ON a.CategoryID = c.CategoryID " +
                "WHERE c.CourseID = ? AND a.Name = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(sqlCheck)) {
            checkStmt.setInt(1, Integer.parseInt(currentClassId));
            checkStmt.setString(2, name);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("Assignment with name '" + name + "' already exists in category '" + category + "' for this course.");
                    return;
                }
            }
        } catch (SQLException e) {
            System.out.println("Error checking for existing assignment: " + e.getMessage());
            return;
        }

        // Insert assignment using prepared statement with JOIN in subquery
        String sqlInsert = "INSERT INTO Assignments (CategoryID, Name, Description, Points) " +
                "VALUES ( (SELECT c.CategoryID FROM Categories c WHERE c.CourseID = ? AND c.Name = ?) , ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sqlInsert)) {
            stmt.setInt(1, Integer.parseInt(currentClassId));
            stmt.setString(2, category);
            stmt.setString(3, name);
            stmt.setString(4, description);
            stmt.setInt(5, points);

            stmt.executeUpdate();
            System.out.println("Assignment '" + name + "' added successfully.");
        } catch (SQLException e) {
            System.out.println("Error adding assignment: "  + e.getMessage());
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

    private void showStudents(String command) throws SQLException {
        if (currentClassId == null) {
            System.out.println("No class is currently selected.");
            return;
        }

        String[] parse = command.split(" ");

        String sql;
        String searchString;

        if (parse.length > 1) {
            sql = "SELECT s.Name " +
                    "FROM Students s " +
                    "INNER JOIN Enrollments e ON s.StudentID = e.StudentID " +
                    "WHERE e.CourseID = ? AND LOWER(s.Name) LIKE ?";
        } else {
            sql = "SELECT s.Name " +
                    "FROM Students s " +
                    "INNER JOIN Enrollments e ON s.StudentID = e.StudentID " +
                    "WHERE e.CourseID = ?";
        }

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, Integer.parseInt(currentClassId));

            if (parse.length > 1) {
                if (command.startsWith("show-students ")) {
                    stmt.setString(2, "%" + parse[1] + "%");
                }
            }

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

    private void assignGrade(String command) throws SQLException {
        if (currentClassId == null) {
            System.out.println("No class is currently selected.");
            return;
        }

        // Extract username, assignment name, and grade from the command string
        String[] parts = command.split(" ", 4); // Split into max 4 parts
        if (parts.length < 4) {
            System.out.println("Invalid command format. Usage: assign-grade <assignment-name> <username>  <grade>");
            return;
        }

        String username = parts[2];
        String assignmentName = parts[1];
        int grade;
        try {
            grade = Integer.parseInt(parts[3]);
        } catch (NumberFormatException e) {
            System.out.println("Invalid grade. Grade must be a number.");
            return;
        }

        // Check if assignment exists in current course and retrieve its ID and max points
        String sql = "SELECT a.AssignmentID, a.Points AS MaxPoints " +
                "FROM Assignments a " +
                "INNER JOIN Categories c ON a.CategoryID = c.CategoryID " +
                "WHERE c.CourseID = ? AND a.Name = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, Integer.parseInt(currentClassId));
            stmt.setString(2, assignmentName);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("Assignment '" + assignmentName + "' not found in this course.");
                    return;
                }

                int assignmentId = rs.getInt("AssignmentID");
                int maxPoints = rs.getInt("MaxPoints");

                if (grade > maxPoints) {
                    System.out.println("WARNING: Grade exceeds maximum points (" + maxPoints + ") for assignment '" + assignmentName + "'.");
                }

                // Insert a new grade entry if student and assignment combination doesn't exist
                sql = "INSERT INTO Grades (StudentID, AssignmentID, Grade) " +
                        "VALUES ((SELECT StudentID FROM Students WHERE Username = ?), ?, ?)";

                try (PreparedStatement insertStmt = conn.prepareStatement(sql)) {
                    insertStmt.setString(1, username);
                    insertStmt.setInt(2, assignmentId);
                    insertStmt.setInt(3, grade);
                    int rowsInserted = insertStmt.executeUpdate();

                    if (rowsInserted == 1) {
                        System.out.println("Grade assigned for student '" + username + "' for assignment '" + assignmentName + "'.");
                    } else {
                        // Handle potential issue (e.g., student might not exist)
                        System.out.println("An unexpected error occurred while assigning the grade.");
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error assigning grade: " + e.getMessage());
        }
    }




    private void showStudentGrades(String command) throws SQLException {
        if (currentClassId == null) {
            System.out.println("No class is currently selected.");
            return;
        }

        String[] parts = command.split(" ");

        String username = parts.length > 1 ? parts[1] : null; // Assuming space after "student-grades "

        if (username == null) {
            System.out.println("Invalid command format. Usage: student-grades <username>");
            return;
        }

        String sql = "SELECT c.Name AS CategoryName, " +
                "SUM(a.Points) AS MaxPointsInCategory, " +
                "COALESCE(SUM(g.Grade * a.Points) / SUM(a.Points), 0) AS CategoryScore " +
                "FROM Categories c " +
                "INNER JOIN Assignments a ON c.CategoryID = a.CategoryID " +
                "LEFT JOIN Grades g ON g.StudentID = (SELECT StudentID FROM Students WHERE Username = ?) " +
                "                    AND g.AssignmentID = a.AssignmentID " +
                "GROUP BY c.Name " +
                "ORDER BY c.Name; ";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                double totalMaxPoints = 0;
                double totalScore = 0;

                System.out.println("Student Grades - " + username);
                System.out.println("------------------------");

                while (rs.next()) {
                    String categoryName = rs.getString("CategoryName");
                    double maxPointsInCategory = rs.getDouble("MaxPointsInCategory");
                    double categoryScore = rs.getDouble("CategoryScore");

                    System.out.println(categoryName + ":");
                    System.out.printf("  * Max Points: %.1f\n", maxPointsInCategory);
                    if (categoryScore > 0) {
                        System.out.printf("  * Score: %.1f (out of %.1f)\n", categoryScore, maxPointsInCategory);
                    } else {
                        System.out.println("  * No grades in this category yet.");
                    }
                    System.out.println();

                    totalMaxPoints += maxPointsInCategory;
                    totalScore += categoryScore;
                }

                System.out.println("------------------------");
                System.out.printf("Total Possible Points: %.1f\n", totalMaxPoints);
                System.out.printf("Total Score: %.1f (out of %.1f)\n", totalScore, totalMaxPoints);
            }
        } catch (SQLException e) {
            System.out.println("Error fetching student grades: " + e.getMessage());
        }
    }


    private void showGradebook() throws SQLException {
        if (currentClassId == null) {
            System.out.println("No class is currently selected.");
            return;
        }

        String sql = "SELECT s.Username, s.StudentID, s.Name, " +
                "COALESCE(SUM(g.Grade * a.Points) / SUM(a.Points), 0) AS TotalScore " +
                "FROM Students s " +
                "INNER JOIN Enrollments e ON s.StudentID = e.StudentID AND e.CourseID = ? " +  // Filter for current course (enrolled)
                "LEFT JOIN Grades g ON g.StudentID = s.StudentID " +  // Student grades
                "LEFT JOIN Assignments a ON g.AssignmentID = a.AssignmentID " +  // Assignments
                "GROUP BY s.Username, s.StudentID, s.Name " +
                "ORDER BY s.Username; ";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, Integer.parseInt(currentClassId));

            try (ResultSet rs = stmt.executeQuery()) {
                System.out.println("Gradebook");
                System.out.println("-----------------------------------------");
                System.out.println("| Username | Student ID | Name           | Total Score |");
                System.out.println("|-----------|------------|----------------|--------------|");

                while (rs.next()) {
                    String username = rs.getString("Username");
                    int studentId = rs.getInt("StudentID");
                    String name = rs.getString("Name");
                    double totalScore = rs.getDouble("TotalScore");

                    System.out.printf("| %-9s | %10d | %-15s | %8.2f        |\n",
                            username, studentId, name, totalScore);
                }

                System.out.println("-----------------------------------------");
            }
        } catch (SQLException e) {
            System.out.println("Error fetching gradebook data: " + e.getMessage());
        }
    }

}
