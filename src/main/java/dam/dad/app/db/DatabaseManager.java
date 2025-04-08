package dam.dad.app.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import org.mindrot.jbcrypt.BCrypt;

public class DatabaseManager {
    private static final String PROPERTIES_FILE = "/database.properties";
    private static DatabaseManager instance;
    private Connection connection;

    private DatabaseManager() {
        try {
            Properties props = new Properties();
            props.load(getClass().getResourceAsStream(PROPERTIES_FILE));
            
            String url = props.getProperty("db.url");
            String username = props.getProperty("db.username");
            String password = props.getProperty("db.password");
            
            connection = DriverManager.getConnection(url, username, password);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public boolean registerUser(String username, String password) {
        if (userExists(username)) {
            return false;
        }

        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        
        String sql = "INSERT INTO usuarios (username, password, email) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, hashedPassword);
            stmt.setString(3, username + "@example.com"); // Email temporal
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean userExists(String username) {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean validateUser(String username, String password) {
        String sql = "SELECT password FROM usuarios WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String hashedPassword = rs.getString("password");
                return BCrypt.checkpw(password, hashedPassword);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
} 