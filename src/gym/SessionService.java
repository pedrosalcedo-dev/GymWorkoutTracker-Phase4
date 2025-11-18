package gym;

import java.sql.*;
import java.util.ArrayList;

// Pedro Salcedo
// CEN 3024 - Phase 4
// Handles database CRUD + helper functions.

public class SessionService {

    private final DBHelper db;

    public SessionService() {
        db = new DBHelper();
    }

    // Connect to a DB file
    public void connect(String path) {
        db.setPath(path);
    }

    // Read all rows
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

    // Add new workout
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

    // Update entire record (used by GUI)
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

    // Delete record
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

    // Check ID
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

    // Simple 1RM check
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

