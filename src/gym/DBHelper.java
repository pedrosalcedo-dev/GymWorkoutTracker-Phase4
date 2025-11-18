package gym;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBHelper {

    private String dbPath = "";

    // Where the DB file is stored
    public void setPath(String path) {
        dbPath = path.trim();
    }

    // Make the actual connection
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
