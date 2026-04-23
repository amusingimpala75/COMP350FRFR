package edu.gcc.hallmonitor;

import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.javalin.Javalin;
import io.javalin.http.Context;

public class AuthController {
    private static final String AUTH_COOKIE = "auth_token";
    private static final long SESSION_TTL_MS = 1000L * 60L * 60L * 24L; // 24 hours
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[A-Za-z0-9_]{3,32}$");
    private static final Map<String, AuthSession> SESSIONS = new ConcurrentHashMap<>();

    private record AuthSession(int id, String username, long expiresAt) {
    }

    private static void cleanupExpiredSessions() {
        long now = System.currentTimeMillis();
        SESSIONS.entrySet().removeIf(entry -> entry.getValue().expiresAt() <= now);
    }

    private static AuthSession createSession(User user) {
        return new AuthSession(user.getId(), user.getUsername(), System.currentTimeMillis() + SESSION_TTL_MS);
    }

    private static void writeAuthCookie(io.javalin.http.Context ctx, String token) {
        String secureFlag = ctx.req().isSecure() ? "; Secure" : "";
        ctx.header("Set-Cookie", AUTH_COOKIE + "=" + token + "; Path=/; HttpOnly; SameSite=Lax" + secureFlag);
    }

    private static void clearAuthCookie(io.javalin.http.Context ctx) {
        String secureFlag = ctx.req().isSecure() ? "; Secure" : "";
        ctx.header("Set-Cookie", AUTH_COOKIE + "=; Path=/; Max-Age=0; HttpOnly; SameSite=Lax" + secureFlag);
    }

    private static String validateCredentials(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            return "Username and password are required";
        }
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            return "Username must be 3-32 chars and use letters, numbers, or underscore";
        }
        if (password.length() < 8) {
            return "Password must be at least 8 characters";
        }
        return null;
    }

    private static ObjectNode toUserJson(AuthSession session) {
        ObjectNode root = Main.MAPPER.createObjectNode();
        root.put("id", session.id());
        root.put("username", session.username());
        return root;
    }

    private static void setUnauthorized(io.javalin.http.Context ctx) {
        ctx.status(401).json(Main.MAPPER.createObjectNode().put("error", "Unauthorized"));
    }

    private static AuthSession getValidSession(Context ctx) {
        cleanupExpiredSessions();
        String token = ctx.cookie(AUTH_COOKIE);
        if (token == null) {
            return null;
        }

        AuthSession session = SESSIONS.get(token);
        if (session == null || session.expiresAt() <= System.currentTimeMillis()) {
            SESSIONS.remove(token);
            clearAuthCookie(ctx);
            return null;
        }

        return session;
    }

    static Integer getAuthenticatedUserId(Context ctx) {
        AuthSession session = getValidSession(ctx);
        return session == null ? null : session.id();
    }

    public static void registerRoutes(Javalin app) {
        app.post("/auth/login", ctx -> {
            cleanupExpiredSessions();
            JsonNode body;
            try {
                body = Main.MAPPER.readTree(ctx.body());
            } catch (Exception ex) {
                ctx.status(400).json(Main.MAPPER.createObjectNode().put("error", "Invalid JSON payload"));
                return;
            }

            String username = body.path("username").asText("").trim();
            String password = body.path("password").asText("");
            String validationError = validateCredentials(username, password);
            if (validationError != null) {
                ctx.status(400).json(Main.MAPPER.createObjectNode().put("error", validationError));
                return;
            }

            try {
                User user = User.login(username, password);
                String token = UUID.randomUUID().toString();
                SESSIONS.put(token, createSession(user));
                writeAuthCookie(ctx, token);
                ctx.json(toUserJson(SESSIONS.get(token)));
            } catch (SecurityException ex) {
                ctx.status(401).json(Main.MAPPER.createObjectNode().put("error", "Invalid credentials"));
            } catch (SQLException ex) {
                ctx.status(500).json(Main.MAPPER.createObjectNode().put("error", "Database error"));
            }
        });

        app.post("/auth/signup", ctx -> {
            cleanupExpiredSessions();
            JsonNode body;
            try {
                body = Main.MAPPER.readTree(ctx.body());
            } catch (Exception ex) {
                ctx.status(400).json(Main.MAPPER.createObjectNode().put("error", "Invalid JSON payload"));
                return;
            }

            String username = body.path("username").asText("").trim();
            String password = body.path("password").asText("");
            String validationError = validateCredentials(username, password);
            if (validationError != null) {
                ctx.status(400).json(Main.MAPPER.createObjectNode().put("error", validationError));
                return;
            }

            try {
                User user = User.signup(username, password);
                String token = UUID.randomUUID().toString();
                SESSIONS.put(token, createSession(user));
                writeAuthCookie(ctx, token);
                ctx.status(201).json(toUserJson(SESSIONS.get(token)));
            } catch (IllegalArgumentException | SecurityException ex) {
                ctx.status(400).json(Main.MAPPER.createObjectNode().put("error", ex.getMessage()));
            } catch (SQLException ex) {
                ctx.status(500).json(Main.MAPPER.createObjectNode().put("error", "Database error"));
            }
        });

        app.get("/auth/me", ctx -> {
            AuthSession session = getValidSession(ctx);
            if (session == null) {
                setUnauthorized(ctx);
                return;
            }

            ctx.json(toUserJson(session));
        });

        app.post("/auth/logout", ctx -> {
            cleanupExpiredSessions();
            String token = ctx.cookie(AUTH_COOKIE);
            if (token != null) {
                SESSIONS.remove(token);
                clearAuthCookie(ctx);
            }
            ctx.status(204);
        });

        app.get("/login", ctx -> ctx.html(Main.readResource("/public/index.html")));
    }
}

