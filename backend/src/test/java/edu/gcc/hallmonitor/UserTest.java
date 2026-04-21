package edu.gcc.hallmonitor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class UserTest {

    public static Connection connection;

    @BeforeAll
    public static void setup() throws SQLException {
        connection = Database.getConnection();

        // Validate the test user is present in the database
        User user = new User("testuser", "password");

        PreparedStatement prepStatement = connection.prepareStatement(
                "SELECT * FROM public.\"users\"" +
                    "WHERE username = ? AND password_hash = ?"
        );
        prepStatement.setString(1, user.getUsername());
        prepStatement.setBytes(2, user.getPasswordHash());
        ResultSet rs = prepStatement.executeQuery();

        assertTrue(rs.next()); // should have a result
    }

    @AfterAll
    public static void tearDown() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Test
    public void isUserTest() {
        User user = null;
        try {
            user = new User("testuser", "password");
        } catch (IllegalArgumentException iae) {
            fail(); // username and password are not empty, so this should not happen
        } catch (SQLException sqle) {
            fail(sqle.getMessage());
        }

        try {
            assertTrue(user.isUser()); // the test user should be in the database and isUser should return true
        } catch (SQLException sqle) {
            fail(sqle.getMessage());
        }
    }

    @Test
    public void emptyUsernameTest() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new User("", "password"));
    }

    @Test
    public void emptyPasswordTest() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new User("testuser", ""));
    }

    @Test
    public void isUsernameTakenTest() {
        User user = null;
        try {
            user = new User("testuser", "notpassword");
        } catch (IllegalArgumentException iae) {
            fail(); // username and password are not empty, so this should not happen
        } catch (SQLException sqle) {
            fail(sqle.getMessage());
        }

        try {
            assertTrue(user.isUsernameTaken());
        } catch (SQLException sqle) {
            fail(sqle.getMessage());
        }
    }

    @Test
    public void addUserWithSameNameTest() {
        User user = null;
        try {
            user = new User("testuser", "notpassword");
        } catch (IllegalArgumentException iae) {
            fail(); // username and password are not empty, so this should not happen
        } catch (SQLException sqle) {
            fail(sqle.getMessage());
        }

        try {
            assertFalse(user.addUser()); // should fail
        } catch (SQLException sqle) {
            fail(sqle.getMessage());
        }
    }

    @Test
    public void addUserTest() {
        // Delete the test user
        User user = null;
        try {
            user = new User("testuser", "password");

            PreparedStatement prepStatement = connection.prepareStatement(
                    "DELETE FROM public.\"users\" WHERE username = ? AND password_hash = ?"
            );
            prepStatement.setString(1, user.getUsername());
            prepStatement.setBytes(2, user.getPasswordHash());
            prepStatement.execute();

        } catch (SQLException sqle) {
            fail(sqle.getMessage());
        }

        // Add the user back
        try {
            assertTrue(user.addUser());

        } catch (SQLException sqle) {
            fail(sqle.getMessage());
        }
    }

    @Test
    public void deleteUserTest() {
        // Delete the test user
        User user;
        try {
            user = new User("testuser", "password");
            assertTrue(user.deleteUser());

            // Add the user back
            PreparedStatement prepStatement = connection.prepareStatement(
                    "INSERT INTO public.\"users\" (username, password_hash)" +
                            "VALUES (?, ?)"
            );
            prepStatement.setString(1, user.getUsername());
            prepStatement.setBytes(2, user.getPasswordHash());
            prepStatement.execute();

        } catch (SQLException sqle) {
            fail(sqle.getMessage());
        }
    }

    @Test
    public void loginUserInDatabase() throws SQLException {
        User.login("testuser", "password");
    }

    @Test
    public void loginUserNotInDatabase() {
        assertThrows(SecurityException.class, () -> User.login("testuser", "notapassword"));
    }

    @Test
    public void signUpUserNotInDatabase() throws SQLException {
        User user = User.signup(getUnusedUsername(), "password");

        assertTrue(user.isUser());

        PreparedStatement prepStatement = connection.prepareStatement(
                "DELETE FROM public.\"users\" WHERE username = ? AND password_hash = ?"
        );
        prepStatement.setString(1, user.getUsername());
        prepStatement.setBytes(2, user.getPasswordHash());
        prepStatement.execute();
    }

    @Test
    public void signUpUserInDatabase() {
        assertThrows(SecurityException.class, () -> User.signup("testuser", "notapassword"));
    }

    @Test
    public void getAndSetGradYearTest() throws SQLException {
        User user = new User("testuser", "password");
        user.setGradYear(2023);
        assertEquals(2023, user.getGradYear());
    }

    public String getUsedUsername() throws SQLException {
        // Get a username in the database
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery(
                "SELECT username FROM public.\"users\" LIMIT 1"
        );
        rs.next();
        return rs.getString(1);
    }

    public String getUnusedUsername() throws SQLException {
        StringBuilder usernameBuilder = new StringBuilder(getUsedUsername());

        User user = null;
        boolean isUsernameTaken = true;

        // If the current one is taken, add an 'a' and check again
        while (isUsernameTaken) {
            user = new User(usernameBuilder.toString(), "1234");

            if (user.isUsernameTaken()) {
                usernameBuilder.append("a");
            } else {
                isUsernameTaken = false;
            }
        }

        return usernameBuilder.toString();
    }
}
