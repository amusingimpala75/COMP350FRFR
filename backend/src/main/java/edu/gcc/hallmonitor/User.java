package edu.gcc.hallmonitor;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class User {
    private String username;
    private byte[] passwordHash;
    private int id;
    private boolean authenticated = false;
    private int gradYear;
    private List<Schedule> schedules;
    private static final Connection CONNECTION;

    static {
        try {
            CONNECTION = Database.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Basic constructor that sets the username and password hash. NOTE: YOU SHOULD USE THE login() or signUp() METHOD
     * FOR CREATING NEW USERS WHICH IN TURN CALLS THIS FUNCTION. YOU SHOULD PRIMARILY USE THIS METHOD BY ITSELF
     * FOR TESTING THE METHODS THAT GO INTO login() and signUp().
     * @param username the username of the user
     * @param password the password of the user
     * @throws IllegalArgumentException if the username or password are empty
     * @throws SQLException if the connection can't be established.
     */
    public User(String username, String password) throws IllegalArgumentException, SQLException, JsonProcessingException {
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
    }

    public User(int userId) throws SQLException {
        PreparedStatement prepStatement = CONNECTION.prepareStatement(
                "SELECT * FROM public.\"users\" WHERE id = ?"
        );
        prepStatement.setInt(1, userId);
        ResultSet rs = prepStatement.executeQuery();

        if (rs.next()) {
            id = userId;
            username = rs.getString("username");
            passwordHash = rs.getBytes("password_hash");
            authenticated = true;
        } else {
            throw new SecurityException("Unknown user");
        }
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

    public static User login(String username, String password) throws SQLException, JsonProcessingException {
        User user = new User(username, password);

        if (user.isUser()) {
            user.id = user.getIdFromDatabase();
            user.authenticated = true;
            user.schedules = user.getUserSchedules();
            return user;
        } else {
            throw new SecurityException("Username and password not found");
        }
    }

    public static User signup(String username, String password) throws SQLException, JsonProcessingException {
        User user = new User(username, password);

        if (user.addUser()) {
            user.id = user.getIdFromDatabase();
            user.authenticated = true;
            user.schedules = user.getUserSchedules();
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
        PreparedStatement prepStatement = CONNECTION.prepareStatement(
                "SELECT * FROM public.\"users\" " +
                    "WHERE username = ? AND password_hash = ?"
        );
        prepStatement.setString(1, username);
        prepStatement.setBytes(2, passwordHash);
        ResultSet rs = prepStatement.executeQuery();

        // If the query result is empty, return false
        return rs.next();
    }

    private int getIdFromDatabase() throws SQLException {
        PreparedStatement prepStatement = CONNECTION.prepareStatement(
                "SELECT id FROM public.\"users\" " +
                    "WHERE username = ? AND password_hash = ?"
        );
        prepStatement.setString(1, username);
        prepStatement.setBytes(2, passwordHash);
        ResultSet rs = prepStatement.executeQuery();
        rs.next();

        return rs.getInt(1);
    }

    /**
     * Checks if the current username member variable is in use in the database
     * @return if the username is already used
     * @throws SQLException if the connection fails
     */
    public boolean isUsernameTaken() throws SQLException {
        PreparedStatement prepStatement = CONNECTION.prepareStatement(
                "SELECT * FROM public.\"users\" " +
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

        PreparedStatement prepStatement = CONNECTION.prepareStatement(
                "INSERT INTO public.\"users\" (username, password_hash, grad_year) " +
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
        PreparedStatement prepStatement = CONNECTION.prepareStatement(
                "DELETE FROM public.\"users\" WHERE username = ? AND password_hash = ?"
        );
        prepStatement.setString(1, username);
        prepStatement.setBytes(2, passwordHash);
        prepStatement.execute();

        // Validate that the username and password have been deleted
        return !isUser();
    }

    public List<Schedule> getUserSchedules() throws SQLException, SecurityException, JsonProcessingException {
        if (!authenticated) {
            throw new SecurityException("User has not be authenticated");
        }

        PreparedStatement prepStatement = CONNECTION.prepareStatement(
                "SELECT id FROM public.\"schedules\" " +
                    "WHERE user_id = ?"
        );
        prepStatement.setInt(1, id);
        ResultSet scheduleIdResultSet = prepStatement.executeQuery();

        List<Integer> scheduleIds = new ArrayList<>();
        while (scheduleIdResultSet.next()) {
            int schedule_id = scheduleIdResultSet.getInt(1);
            scheduleIds.add(schedule_id);
        }

        List<Schedule> schedules = new ArrayList<>();
        for (int scheduleId: scheduleIds) {
            Schedule schedule = Schedule.loadSchedule(id, scheduleId);
            schedules.add(schedule);
        }

        return schedules;
    }

    public void addSchedule() throws SQLException, JsonProcessingException {
        PreparedStatement prepStatement = CONNECTION.prepareStatement(
                "INSERT INTO public.\"schedules\" (user_id) VALUES (?)",
                Statement.RETURN_GENERATED_KEYS
        );
        prepStatement.setInt(1, id);
        prepStatement.execute();
        ResultSet generatedKey = prepStatement.getGeneratedKeys();
        if (generatedKey.next()) {
            int scheduleId = generatedKey.getInt(1);

            schedules.add(Schedule.loadSchedule(id, scheduleId));
        }
    }

    public void removeSchedule(int scheduleId) throws SQLException {
        PreparedStatement prepStatement = CONNECTION.prepareStatement(
                "DELETE FROM public.\"schedules\" WHERE id = ?"
        );
        prepStatement.setInt(1, scheduleId);
        prepStatement.execute();

        schedules.removeIf((schedule) -> schedule.getId() == scheduleId);
    }
}
