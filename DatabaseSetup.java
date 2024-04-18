import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseSetup {

    public static void main(String[] args) {
           // Explicitly load the MySQL JDBC driver to handle environments where automatic driver loading is not happening.
           try {
            Class.forName("com.mysql.cj.jdbc.Driver"); // Ensure the driver is registered
        } catch (ClassNotFoundException e) {
            System.out.println("MySQL JDBC driver not found.");
            e.printStackTrace();
            return; // Exit if there is no JDBC driver found
        }
        // Corrected JDBC connection string to reflect the given MySQL hostname and port
        String url = "jdbc:mysql://127.0.0.1:50007/finalproject"; // Base URL for MySQL with specified port and directly connect to the 'finalproject' database
        String user = "msandbox"; // provided MySQL username
        String password = "REPLACEME"; // the password

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            try (Statement stmt = conn.createStatement()) {
                // Create the database if it does not already exist
                stmt.execute("CREATE DATABASE IF NOT EXISTS finalproject");
                stmt.execute("USE finalproject");

                // Create the Courses table
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Courses (" +
                        "CourseID INT AUTO_INCREMENT PRIMARY KEY," +
                        "CourseNumber VARCHAR(10) NOT NULL," +
                        "Term VARCHAR(10) NOT NULL," +
                        "SectionNumber INT NOT NULL," +
                        "Description TEXT," +
                        "UNIQUE (CourseNumber, Term, SectionNumber)" +
                        ")");

                // Create the Categories table
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Categories (" +
                        "CategoryID INT AUTO_INCREMENT PRIMARY KEY," +
                        "CourseID INT NOT NULL," +
                        "Name VARCHAR(50) NOT NULL," +
                        "Weight DECIMAL(5,2) NOT NULL," +
                        "FOREIGN KEY (CourseID) REFERENCES Courses(CourseID) ON DELETE CASCADE," +
                        "UNIQUE (CourseID, Name)" +
                        ")");

                // Create the Assignments table
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Assignments (" +
                        "AssignmentID INT AUTO_INCREMENT PRIMARY KEY," +
                        "CategoryID INT NOT NULL," +
                        "Name VARCHAR(255) NOT NULL," +
                        "Description TEXT," +
                        "Points INT NOT NULL," +
                        "FOREIGN KEY (CategoryID) REFERENCES Categories(CategoryID) ON DELETE CASCADE," +
                        "UNIQUE (CategoryID, Name)" +
                        ")");

                // Create the Students table
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Students (" +
                        "StudentID INT AUTO_INCREMENT PRIMARY KEY," +
                        "Username VARCHAR(100) NOT NULL UNIQUE," +
                        "Name VARCHAR(100) NOT NULL" +
                        ")");

                // Create the Enrollments table
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Enrollments (" +
                        "StudentID INT NOT NULL," +
                        "CourseID INT NOT NULL," +
                        "FOREIGN KEY (StudentID) REFERENCES Students(StudentID) ON DELETE CASCADE," +
                        "FOREIGN KEY (CourseID) REFERENCES Courses(CourseID) ON DELETE CASCADE," +
                        "PRIMARY KEY (StudentID, CourseID)" +
                        ")");

                // Create the Grades table
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Grades (" +
                        "StudentID INT NOT NULL," +
                        "AssignmentID INT NOT NULL," +
                        "Grade DECIMAL(5,2) NOT NULL," +
                        "FOREIGN KEY (StudentID) REFERENCES Students(StudentID) ON DELETE CASCADE," +
                        "FOREIGN KEY (AssignmentID) REFERENCES Assignments(AssignmentID) ON DELETE CASCADE," +
                        "PRIMARY KEY (StudentID, AssignmentID)" +
                        ")");

                System.out.println("Database and tables created successfully.");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
