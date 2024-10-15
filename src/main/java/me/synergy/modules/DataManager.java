package me.synergy.modules;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import me.synergy.brains.Synergy;
import me.synergy.objects.DataObject;

public class DataManager {

    private static Connection connection;
    private static Map<UUID, CachedData> cachedData = new ConcurrentHashMap<>();
    
    public void initialize() {
        try {
	    	String dbType = Synergy.getConfig().getString("storage.type");
	    	String host = Synergy.getConfig().getString("storage.host");
	    	String database = Synergy.getConfig().getString("storage.database");
	    	String user = Synergy.getConfig().getString("storage.user");
	    	String port = Synergy.getConfig().getString("storage.port");
	    	String password = Synergy.getConfig().getString("storage.password");
	    	
	        if (dbType.equalsIgnoreCase("sqlite")) {
	            connection = DriverManager.getConnection("jdbc:sqlite:" + host);
	        } else if (dbType.equalsIgnoreCase("mysql")) {
	            connection = DriverManager.getConnection("jdbc:mysql://" + host+":"+port+"/"+database, user, password);
	        }
	        createTable();
	        Synergy.getLogger().info(String.valueOf(getClass().getSimpleName()) + " module has been initialized!");
        } catch (Exception c) {
	        Synergy.getLogger().info(String.valueOf(getClass().getSimpleName()) + " module has been initialized!");
        }
    }

    private void createTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS synergy (" +
                "uuid VARCHAR(36) NOT NULL," +
                "option VARCHAR(255) NOT NULL," +
                "value TEXT," +
                "PRIMARY KEY (uuid, option)" +
                ")";
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }

    static class CachedData {
        private static final String NULL = "";
        private final Map<String, String> options = new ConcurrentHashMap<>();

        public boolean hasOption(String option) {
            return options.containsKey(option);
        }
        
        public String getOption(String option) {
        	String value = options.get(option);
            return hasOption(option) && !value.equals(NULL) ? value : null;
        }
        
        public void setOption(String option, String value) {
    		options.put(option, value != null ? value : NULL);
        }
    }

    public String getData(UUID uuid, String option) throws SQLException {
    	return getData(uuid, option, true);
    }
    
    public String getData(UUID uuid, String option, boolean cache) throws SQLException {
    	if (cache) {
	    	CachedData entry = cachedData.get(uuid);
	        if (entry != null && entry.hasOption(option)) {
	        	return entry.getOption(option);
	        }
    	}
        String sql = "SELECT value FROM synergy WHERE uuid = ? AND option = ?";
        String value = null;
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            pstmt.setString(2, option);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
            	value = rs.getString("value");
            }
            cachedData.computeIfAbsent(uuid, k -> new CachedData())
                      .setOption(option, value);
        }
        return value;
    }
    
    public void setData(UUID uuid, String option, String value) throws SQLException {
        String sql = value == null ? "DELETE FROM synergy WHERE uuid = ? AND option = ?"
        						   : "REPLACE INTO synergy (uuid, option, value) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            pstmt.setString(2, option);
            if (value != null) {
                pstmt.setString(3, value);
            }
            pstmt.executeUpdate();
            cachedData.computeIfAbsent(uuid, k -> new CachedData())
                      .setOption(option, value);
        }
    }
    
    public UUID findUserUUID(String option, String value) throws SQLException {
        String sql = "SELECT uuid FROM synergy WHERE option = ? AND value = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, option);
            pstmt.setString(2, value);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new DataObject(rs.getString("uuid")).getAsUUID();
            } else {
                return null;
            }
        }
    }
    
    public void clearCache(UUID uuid) {
    	cachedData.remove(uuid);
    }

    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
    
}
