package burp;

import burp.api.montoya.*;
import burp.api.montoya.http.handler.*;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.message.responses.HttpResponse;
import java.sql.*;

public class BurpExtender implements BurpExtension, HttpHandler {

    private Connection connection;
    private MontoyaApi api;

    @Override
    public void initialize(MontoyaApi api) {
        this.api = api;
        api.extension().setName("SQLite Logger");

        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            api.logging().logToError("SQLite JDBC driver not found: " + e.getMessage());
            return;
        }

        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + System.getProperty("user.home") + "/burp_requests.db");
            Statement stmt = connection.createStatement();
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS requests (
                    tool TEXT,
                    status INT,
                    host TEXT,
                    method TEXT,
                    path TEXT,
                    req TEXT,
                    res TEXT,
                    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP
                )
            """);
            stmt.close();
            api.logging().logToOutput("Database initialized successfully");
        } catch (SQLException e) {
            api.logging().logToError("Failed to connect to SQLite DB: " + e.getMessage());
            return;
        }

        // Register the HTTP handler
        api.http().registerHttpHandler(this);
        api.logging().logToOutput("Global Request Logger initialized");
    }

    private void logHttpMessage(String tool, HttpRequest request, HttpResponse response) {
        String host = request.httpService().host();
        String method = request.method();
        String path = request.path();
        String req = request.toString();
        String res = response != null ? response.toString() : "";
        short status = response != null ? response.statusCode() : 0;

        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO requests (tool, status, host, method, path, req, res) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
            pstmt.setString(1, tool);
            pstmt.setShort(2, status);
            pstmt.setString(3, host);
            pstmt.setString(4, method);
            pstmt.setString(5, path);
            pstmt.setString(6, req);
            pstmt.setString(7, res);
            pstmt.executeUpdate();
            api.logging().logToOutput("Logged " + tool + " request to " + host + path);
        } catch (SQLException e) {
            api.logging().logToError("Database error: " + e.getMessage());
        }
    }

    @Override
    public RequestToBeSentAction handleHttpRequestToBeSent(HttpRequestToBeSent requestToBeSent) {
        try {
            // Log the request when it's sent
            String toolName = requestToBeSent.toolSource().toolType().toolName();
            logHttpMessage(toolName, requestToBeSent, null);
        } catch (Exception e) {
            api.logging().logToError("Error in handleHttpRequestToBeSent: " + e.getMessage());
        }
        
        // Let the request continue without modification
        return RequestToBeSentAction.continueWith(requestToBeSent);
    }

    @Override
    public ResponseReceivedAction handleHttpResponseReceived(HttpResponseReceived responseReceived) {
        try {
            // Log the complete request/response pair
            String toolName = responseReceived.toolSource().toolType().toolName();
            logHttpMessage(toolName, responseReceived.initiatingRequest(), responseReceived);
        } catch (Exception e) {
            api.logging().logToError("Error in handleHttpResponseReceived: " + e.getMessage());
        }
        
        // Let the response continue without modification
        return ResponseReceivedAction.continueWith(responseReceived);
    }
}