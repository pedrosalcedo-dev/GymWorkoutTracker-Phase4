package gym;

/**
 * Represents a single workout session in the system.
 * This class just stores the data for one record.
 */
public class WorkoutSession {

    /** Unique ID for the workout session. */
    public int id;

    /** Date of the workout (yyyy-mm-dd). */
    public String date;

    /** Name of the exercise performed. */
    public String exerciseName;

    /** Muscle group targeted during the exercise. */
    public String muscleGroup;

    /** Number of sets completed. */
    public int sets;

    /** Number of reps performed. */
    public int reps;

    /** Weight used (in pounds). */
    public double weightLbs;

    /** Duration of the workout in minutes. */
    public int durationMin;

    /** RPE value (1–10). */
    public int rpe;

    /** Optional notes for the session. */
    public String notes;

    /**
     * Returns the workout information as a CSV-style line.
     * @return a comma-separated string with the session data
     */
    @Override
    public String toString() {
        return id + "," + date + "," + exerciseName + "," + muscleGroup + "," +
                sets + "," + reps + "," + weightLbs + "," + durationMin + "," + rpe + "," + notes;
    }
}
