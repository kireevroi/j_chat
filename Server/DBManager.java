/*kireevroi 2022*/

/*Implementing the database that stores the names and password hashes inside.
  Passwords are not stored raw. Implemented via jDBc and sqlite.
  Stored server side.                                                         */

package server;

import java.sql.*;

public class DBManager {
    // The database connection
    private Connection connection = null;
    // Constructor
    public DBManager() {
        try {
            // Load the driver
            Class.forName("org.sqlite.JDBC");
            this.connection = DriverManager.getConnection("jdbc:sqlite:server.db");
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
        System.out.println("Opened database successfully");
    }
    // Creating the table
    public void createTable() {
        try {
            Statement statement = connection.createStatement();
            // Checking for table existance before creating
            String sql = "CREATE TABLE IF NOT EXISTS USERS " +
                         "(NAME TEXT NOT NULL, "  +
                         "PASSWORD INT, " +
                         "UNIQUE(NAME))";
            statement.executeUpdate(sql);
            statement.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
        System.out.println("Table opened/created");
    }
    // Adding a user into the table
    public void createUser(String name, int hashPass) {
        try {
            Statement statement = connection.createStatement();
            //inserting new user if not exists
            String sql = "INSERT OR IGNORE INTO" +
                         " USERS (NAME, PASSWORD) VALUES('" +
                         name + "', " + hashPass +")";
            statement.executeUpdate(sql);
            statement.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
            System.out.println("Added/got " + name + " if not existed");
    }
    // Getting a hashsum for user
    public int getUser(String name) {
        int hashPass = 0;
        try {
            Statement statement = connection.createStatement();
            String sql = "SELECT PASSWORD FROM USERS WHERE NAME = '" +
            name + "'";
            ResultSet res = statement.executeQuery(sql);
            if (res.next()) {
                hashPass = res.getInt("PASSWORD");
                System.out.println("Got password for " + name + " from DB");
            }
            statement.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
        return hashPass;
    }

}
