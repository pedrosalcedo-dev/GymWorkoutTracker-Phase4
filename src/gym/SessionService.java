package gym;

import java.sql.*;
import java.util.ArrayList;

/**
 * Handles all database operations for the workout tracker.
 * This includes reading, adding, updating, deleting records,
 * and a helper method for calculating estimated 1RM values.
 */
public class SessionService {

    /** Helper class that manages the SQLite connection. */
    private final DBHelper db;

    /** Creates a new service with a DBHelper. */
    public SessionService() {
        db = new DBHelper();
    }

    /**
     * Sets the database file path so the app can connect.
     * @param path full path to the .db file
     */
    public void connect(String path) {
        db.setPath(path);
    }

    /**
     * Loads all workout rows from the database.
     * @return a list of all workout sessions
     */
    public ArrayList<WorkoutSession> listAll() {
        ArrayList<WorkoutSession> list = new ArrayList<>();
        String sql = "SELECT * FROM workouts ORDER BY id";

        try (Connection conn = db.connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                WorkoutSession s = new WorkoutSession();
                s.id = rs.getInt("id");
                s.date = rs.getString("date");
                s.exerciseName = rs.getString("exercise");
                s.muscleGroup = rs.getString("muscle");
                s.sets = rs.getInt("sets");
                s.reps = rs.getInt("reps");
                s.weightLbs = rs.getDouble("weight");
                s.durationMin = rs.getInt("duration");
                s.rpe = rs.getInt("rpe");
                s.notes = rs.getString("notes");
                list.add(s);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    /**
     * Adds a new workout record to the database.
     * @param s the workout session to insert
     * @return true if the insert worked, false if something failed
     */
    public boolean add(WorkoutSession s) {
        String sql = "INSERT INTO workouts (id, date, exercise, muscle, sets, reps, weight, duration, rpe, notes) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = db.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, s.id);
            ps.setString(2, s.date);
            ps.setString(3, s.exerciseName);
            ps.setString(4, s.muscleGroup);
            ps.setInt(5, s.sets);
            ps.setInt(6, s.reps);
            ps.setDouble(7, s.weightLbs);
            ps.setInt(8, s.durationMin);
            ps.setInt(9, s.rpe);
            ps.setString(10, s.notes);

            ps.executeUpdate();
            return true;

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Updates all fields of an existing workout record.
     * Used when the user edits a row in the GUI.
     * @param s the updated record
     * @return true if the update worked, false if not
     */
    public boolean updateSession(WorkoutSession s) {
        String sql = "UPDATE workouts SET date=?, exercise=?, muscle=?, sets=?, reps=?, weight=?, duration=?, rpe=?, notes=? "
                + "WHERE id=?";

        try (Connection conn = db.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, s.date);
            ps.setString(2, s.exerciseName);
            ps.setString(3, s.muscleGroup);
            ps.setInt(4, s.sets);
            ps.setInt(5, s.reps);
            ps.setDouble(6, s.weightLbs);
            ps.setInt(7, s.durationMin);
            ps.setInt(8, s.rpe);
            ps.setString(9, s.notes);
            ps.setInt(10, s.id);

            ps.executeUpdate();
            return true;

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Deletes a workout record by ID.
     * @param id the record to delete
     * @return true if it deleted successfully
     */
    public boolean deleteById(int id) {
        String sql = "DELETE FROM workouts WHERE id=?";

        try (Connection conn = db.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
            return true;

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Checks if a specific ID already exists in the database.
     * @param id the ID to check
     * @return true if found
     */
    public boolean existsId(int id) {
        String sql = "SELECT id FROM workouts WHERE id=?";

        try (Connection conn = db.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next();

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Looks at the last 7 days for the same exercise and
     * returns the best estimated 1RM found.
     * @param exercise name of the lift
     * @param date date to compare against
     * @return highest estimated 1RM (or 0 if none found)
     */
    public double bestE1RMInLast7Days(String exercise, String date) {
        String sql = """
                SELECT sets, reps, weight FROM workouts 
                WHERE exercise = ? AND date >= date(?, '-7 day')
                """;

        double best = 0;

        try (Connection conn = db.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, exercise);
            ps.setString(2, date);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                double w = rs.getDouble("weight");
                int r = rs.getInt("reps");
                double est = w * (1 + r / 30.0);
                if (est > best) best = est;
            }

        } catch (Exception ignore) {}

        return best;
    }
}
