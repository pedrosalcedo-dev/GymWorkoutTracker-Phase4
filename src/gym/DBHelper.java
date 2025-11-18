package gym;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * Simple helper class that manages the SQLite database connection.
 * The GUI and service classes use this to open the database file.
 */
public class DBHelper {

    /** Path to the SQLite .db file. */
    private String dbPath = "";

    /**
     * Sets the file path for the database.
     * @param path the full path to the .db file
     */
    public void setPath(String path) {
        dbPath = path.trim();
    }

    /**
     * Opens a connection to the database.
     * @return a valid SQLite connection
     * @throws IllegalArgumentException if the path is missing or connection fails
     */
    public Connection connect() {
        try {
            if (dbPath.isEmpty())
                throw new IllegalArgumentException("Database path is empty.");

            String url = "jdbc:sqlite:" + dbPath;
            return DriverManager.getConnection(url);

        } catch (Exception e) {
            throw new IllegalArgumentException("Could not connect to: " + dbPath);
        }
    }
}
