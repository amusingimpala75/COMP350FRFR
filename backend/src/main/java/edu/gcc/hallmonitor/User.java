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

    public User() {

    }

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

    public void setGradYear(int gradYear) {
        this.gradYear = gradYear;
    }

    public static User authenticate(String username, String password) throws Exception {
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

    public boolean addUser() {
        return false;
    }
}
