package sql;

import java.sql.*;

public class DatabaseSetup {

    /**
     * Creates a database connection with the port number and the password to your database being passed through
     * It uses the onyx SQL server provided to us, so the username is automatically msandbox
     * The database we use is called finalproject
     * It uses the port that is assigned to each users' respective SQL server on onyx
     * In order to establish a connection you have to foward the database port using an SSH connection.
     * Read the README for a more detailed setup
     *
     * @return java.sql.Connection
     * @throws SQLException
     */
    public static Connection getDatabaseConnection(int port, String password) throws SQLException {
        int databasePort = port;
        String databaseHost = "localhost";
        String databaseUsername = "msandbox";
        String databasePassword = password;
        String databaseName = "finalproject";
        String databaseURL = String.format(
                "jdbc:mysql://%s:%s/%s?verifyServerCertificate=false&useSSL=true&serverTimezone=UTC",
                databaseHost,
                databasePort,
                databaseName);

        try {

            return DriverManager.getConnection(databaseURL, databaseUsername, databasePassword);
        } catch (SQLException sqlException) {
            System.out.printf("SQLException was thrown while trying to connection to database: %s%n", databaseURL);
            System.out.println(sqlException.getMessage());
            throw sqlException;
        }

    }
}
