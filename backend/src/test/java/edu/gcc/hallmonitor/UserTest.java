package edu.gcc.hallmonitor;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

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
            fail("Unable to establish connection with database");
        }

        try {
            assertTrue(user.isUser()); // the test user should be in the database and isUser should return true
        } catch (SQLException sqle) {
            fail("Unable to establish database connection");
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
            fail("Unable to establish connection with database");
        }

        try {
            assertTrue(user.isUsernameTaken());
        } catch (SQLException sqle) {
            fail("Unable to establish database connection");
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
            fail("Unable to establish connection with database");
        }

        try {
            assertFalse(user.addUser()); // should fail
        } catch (SQLException sqle) {
            fail("Unable to establish database connection");
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
            fail("Unable to establish database connection");
        }

        // Add the user back
        try {
            assertTrue(user.addUser());
            // Change the id to 1 so that it doesn't go up by 1 each test
            PreparedStatement prepStatement = connection.prepareStatement(
                    "UPDATE public.\"users\" SET id = ? WHERE username = ? AND password_hash = ?"
            );
            prepStatement.setInt(1, 1);
            prepStatement.setString(2, user.getUsername());
            prepStatement.setBytes(3, user.getPasswordHash());

        } catch (SQLException sqle) {
            fail("Unable to establish database connection");
        }
    }

    @Test
    public void deleteUserTest() {
        // Delete the test user
        User user = null;
        try {
            user = new User("testuser", "password");
            assertTrue(user.deleteUser());

            // Add the user back
            Connection connection = Database.getConnection();
            PreparedStatement prepStatement = connection.prepareStatement(
                    "INSERT INTO public.\"users\" (id, username, password_hash)" +
                            "VALUES (?, ?, ?)"
            );
            prepStatement.setInt(1, 1); // We don't want the tests to add 1 to the id every time
            prepStatement.setString(2, user.getUsername());
            prepStatement.setBytes(3, user.getPasswordHash());
            prepStatement.execute();


        } catch (SQLException sqle) {
            fail("Unable to establish database connection");
        }

    }
}
