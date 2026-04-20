package edu.gcc.hallmonitor;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class User {
    private String username;
    private byte[] passwordHash;
    private int gradYear;
    private Connection connection;

    /**
     * Basic constructor that sets the username and password hash. NOTE: YOU SHOULD USE THE authenticate() METHOD
     * FOR CREATING NEW USERS WHICH IN TURN CALLS THIS FUNCTION. YOU SHOULD PRIMARILY USE THIS METHOD BY ITSELF
     * FOR TESTING THE METHODS THAT GO INTO authenticate().
     * @param username the username of the user
     * @param password the password of the user
     * @throws IllegalArgumentException if the username or password are empty
     * @throws SQLException if the connection can't be established.
     */
    public User(String username, String password) throws IllegalArgumentException, SQLException {
        if (password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        } else if (username.isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        this.username = username;

        try {
            MessageDigest digest = MessageDigest.getInstance("sha256");
            passwordHash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException ignored) {
            throw new IllegalArgumentException("sha256 not found");
        } // won't fail since sha256 is hardcoded

        connection = Database.getConnection();
    }

    public String getUsername() {
        return username;
    }

    public byte[] getPasswordHash() {
        return passwordHash;
    }

    public int getGradYear() {
        return gradYear;
    }

    public void setGradYear(int gradYear) {
        this.gradYear = gradYear;
    }

    public static User login(String username, String password) throws SQLException {
        User user = new User(username, password);

        if (user.isUser()) {
            return user;
        } else {
            throw new SecurityException("Username and password not found");
        }
    }

    public static User signup(String username, String password) throws SQLException {
        User user = new User(username, password);

        if (user.addUser()) {
            return user;
        } else {
            throw new SecurityException("Username and password are taken");
        }
    }

    /**
     * Checks to see if the user with username and password_hash are in the database
     * @return if the username and password match a user in the database
     * @throws SQLException if the connection fails
     */
    public boolean isUser() throws SQLException {
        // Get all the users that match the current user (should be at max 1)
        PreparedStatement prepStatement = connection.prepareStatement(
                "SELECT * FROM public.\"users\"" +
                    "WHERE username = ? AND password_hash = ?"
        );
        prepStatement.setString(1, username);
        prepStatement.setBytes(2, passwordHash);
        ResultSet rs = prepStatement.executeQuery();

        // If the query result is empty, return false
        return rs.next();
    }

    /**
     * Checks if the current username member variable is in use in the database
     * @return if the username is already used
     * @throws SQLException if the connection fails
     */
    public boolean isUsernameTaken() throws SQLException {
        PreparedStatement prepStatement = connection.prepareStatement(
                "SELECT * FROM public.\"users\"" +
                    "WHERE username = ?"
        );
        prepStatement.setString(1, username);
        ResultSet rs = prepStatement.executeQuery();

        // If the query result is empty, return false
        return rs.next();
    }

    /**
     * Adds a user if the username is not already taken
     * @return if the user was successfully added
     * @throws SQLException if the connection fails
     */
    public boolean addUser() throws SQLException {
        // Two users should not share a username
        if (isUsernameTaken()) {
            return false;
        }

        PreparedStatement prepStatement = connection.prepareStatement(
                "INSERT INTO public.\"users\" (username, password_hash, grad_year)" +
                    "VALUES (?, ?, ?)"
        );
        prepStatement.setString(1, username);
        prepStatement.setBytes(2, passwordHash);
        prepStatement.setInt(3, gradYear);
        prepStatement.execute();

        // Validate that the username and password have been added
        return isUser();
    }

    /**
     * Deletes a user from the database. If a user is not in the database, then this method returns false
     * @return if the user is no longer present in the database
     * @throws SQLException if the connection fails
     */
    public boolean deleteUser() throws SQLException {
        if (!isUser()) {
            return false;
        }
        PreparedStatement prepStatement = connection.prepareStatement(
                "DELETE FROM public.\"users\" WHERE username = ? AND password_hash = ?"
        );
        prepStatement.setString(1, username);
        prepStatement.setBytes(2, passwordHash);
        prepStatement.execute();

        // Validate that the username and password have been deleted
        return !isUser();
    }
}
