package edu.gcc.hallmonitor;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;

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
        } catch (NoSuchAlgorithmException ignored) {} // won't fail since sha256 is hardcoded

        connection = Database.getConnection();
    }

    public String getUsername() {
        return username;
    }

    public byte[] getPasswordHash() {
        return passwordHash;
    }

    public void setGradYear(int gradYear) {
        this.gradYear = gradYear;
    }

    /**
     * Validate that the username and password are in the database. If they are not, validate that the username
     * is not taken already. If it is not, add the user. The user object is then returned.
     * @param username the username to authenticate
     * @param password the password to authenticate
     * @return an authenticated User object
     * @throws IllegalArgumentException if the username or password are empty
     * @throws SecurityException if the user cannot be authenticated
     * @throws SQLException if there is an error with the database connection
     */
    public static User authenticate(String username, String password) throws IllegalArgumentException, SecurityException, SQLException {
        User user = new User(username, password);

        // If they are not a user and they can't successfully be added, throw an error
        if (!user.isUser() && !user.addUser()) {
            throw new SecurityException(
                    String.format("Unable to add user '%s'", username)
            );
        }

        return user;
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

    public boolean deleteUser() throws SQLException {
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
