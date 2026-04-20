package edu.gcc.hallmonitor;

import io.github.cdimascio.dotenv.Dotenv;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    public static final Dotenv DOTENV = Dotenv.configure()
            .directory(".")
            .ignoreIfMissing()
            .load();

    public static Connection getConnection() throws SQLException {
        String url = DOTENV.get("DB_URL");
        String user = DOTENV.get("DB_USER");
        String password = DOTENV.get("DB_PASSWORD");

        return DriverManager.getConnection(url, user, password);
    }
}
