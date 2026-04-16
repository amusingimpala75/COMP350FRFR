package edu.gcc.hallmonitor;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class User {
    private String username;
    private byte[] passwordHash;
    private int gradYear;

    public User(String username, String password) throws NoSuchAlgorithmException {
        this.username = username;

        MessageDigest digest = MessageDigest.getInstance("sha256");
        passwordHash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
    }

    public void setGradYear(int gradYear) {
        this.gradYear = gradYear;
    }

    public boolean isUser() {
        return false;
    }

    public boolean addUser(String username, byte[] passwordHash) {
        return false;
    }
}
