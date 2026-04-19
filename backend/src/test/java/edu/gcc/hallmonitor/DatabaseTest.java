package edu.gcc.hallmonitor;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class DatabaseTest {
    @Test
    public void testDatabaseConnection() throws SQLException {
        Connection conn = Database.getConnection();
        Statement statement = conn.createStatement();
        ResultSet rs = statement.executeQuery("SELECT * FROM public.\"users\"");
        assertTrue(rs.next()); // Should have a result
    }
}
