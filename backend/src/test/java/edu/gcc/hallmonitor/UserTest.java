package edu.gcc.hallmonitor;

import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

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
}
