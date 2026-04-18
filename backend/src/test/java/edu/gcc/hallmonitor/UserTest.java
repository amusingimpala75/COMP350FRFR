package edu.gcc.hallmonitor;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;

public class UserTest {
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
        Connection connection = null;
        try {
            user = new User("testuser", "password");

            connection = Database.getConnection();
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
            Connection connection = Database.getConnection();
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
    public void authenticateCurrentUser() {
        try {
            User.authenticate("testuser", "password");
        } catch (IllegalArgumentException iae) {
            fail(); // username and password aren't empty, so shouldn't happen
        } catch (SecurityException se) {
            fail(); // username and password should be in the database, so shouldn't happen
        } catch (SQLException sqle) {
            fail(sqle.getMessage());
        }
    }

    @Test
    public void authenticateNewUser() {
        String username;
        try {
            username = getUnusedUsername();
            User newUser = User.authenticate(username, "1234");

            assertTrue(newUser.isUser());

            // Delete the user we added
            Connection connection = Database.getConnection();
            PreparedStatement prepStatement = connection.prepareStatement(
                    "DELETE FROM public.\"users\" WHERE username = ? AND password_hash = ?"
            );
            prepStatement.setString(1, newUser.getUsername());
            prepStatement.setBytes(2, newUser.getPasswordHash());
            prepStatement.execute();

        } catch (SQLException sqle) {
            fail(sqle.getMessage());
        }

    }

    public String getUnusedUsername() throws SQLException {
        // Get a username in the database
        Connection conn = Database.getConnection();
        Statement statement = conn.createStatement();
        ResultSet rs = statement.executeQuery(
                "SELECT username FROM public.\"users\" LIMIT 1"
        );
        rs.next();
        StringBuilder usernameBuilder = new StringBuilder(rs.getString(1));

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
