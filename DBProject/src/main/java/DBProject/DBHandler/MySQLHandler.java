package DBProject.DBHandler;

import DBProject.DBConnect.MySQLConnection;
import DBProject.DBModels.Provider;
import DBProject.DBModels.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MySQLHandler {

    // Inserts a provider into the MySQL database and returns the generated ID
    public static int insertProvider(Provider provider) throws SQLException {
        String sql = "INSERT INTO providers (name, email) VALUES (?, ?)";
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, provider.getName());
            stmt.setString(2, provider.getEmail());
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1); // Return generated provider ID
            }
        }
        return -1;
    }

    // Inserts a service for a given provider ID
    public static void insertService(int providerId, Service service) throws SQLException {
        // Only insert the service if it is not null and has meaningful data
        if (service != null && service.getName() != null && !service.getName().isEmpty()) {
            String sql = "INSERT INTO services (provider_id, name, description) VALUES (?, ?, ?)";
            try (Connection conn = MySQLConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, providerId);
                stmt.setString(2, service.getName());
                stmt.setString(3, service.getDescription());
                stmt.executeUpdate();
            }
        }
    }

    // Checks if a provider exists in the database by name and email
    public static boolean providerExists(String name, String email) throws SQLException {
        String sql = "SELECT id FROM providers WHERE name = ? AND email = ?";
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setString(2, email);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }
    }

    // Retrieves the provider ID based on name and email
    public static int getProviderId(String name, String email) throws SQLException {
        String sql = "SELECT id FROM providers WHERE name = ? AND email = ?";
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setString(2, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        }
        return -1;
    }

    // Lists all services for a specific provider
    public static List<Service> listServicesByProvider(String providerName, String providerEmail) throws SQLException {
        List<Service> services = new ArrayList<>();
        String sql = "SELECT s.name, s.description FROM services s " +
                "JOIN providers p ON s.provider_id = p.id " +
                "WHERE p.name = ? AND p.email = ?";
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, providerName);
            stmt.setString(2, providerEmail);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                services.add(new Service(rs.getString("name"), rs.getString("description")));
            }
        }
        return services;
    }

    // Updates provider information
    public static void updateProvider(int providerId, String newName, String newEmail) throws SQLException {
        String sql = "UPDATE providers SET name = ?, email = ? WHERE id = ?";
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newName);
            stmt.setString(2, newEmail);
            stmt.setInt(3, providerId);
            stmt.executeUpdate();
        }
    }

    // Updates a service for a specific provider
    public static void updateService(String providerName, String providerEmail, String oldServiceName, Service updatedService) throws SQLException {
        String sql = "UPDATE services SET name = ?, description = ? " +
                "WHERE name = ? AND provider_id = (SELECT id FROM providers WHERE name = ? AND email = ?)";
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, updatedService.getName());
            stmt.setString(2, updatedService.getDescription());
            stmt.setString(3, oldServiceName);
            stmt.setString(4, providerName);
            stmt.setString(5, providerEmail);
            stmt.executeUpdate();
        }
    }

    // Deletes a service for a specific provider
    public static void deleteService(String providerName, String providerEmail, String serviceName) throws SQLException {
        String sql = "DELETE FROM services WHERE name = ? " +
                "AND provider_id = (SELECT id FROM providers WHERE name = ? AND email = ?)";
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, serviceName);
            stmt.setString(2, providerName);
            stmt.setString(3, providerEmail);
            stmt.executeUpdate();
        }
    }
}