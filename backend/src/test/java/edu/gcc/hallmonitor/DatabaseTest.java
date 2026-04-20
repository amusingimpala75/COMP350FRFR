package edu.gcc.hallmonitor;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DatabaseTest {
    @Test
    public void testDatabaseConnection() throws SQLException {
        assertNotNull(Database.getConnection());
    }
}
