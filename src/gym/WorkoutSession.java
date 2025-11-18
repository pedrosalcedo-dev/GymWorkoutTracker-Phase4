package gym;

public class WorkoutSession {
    public int id;
    public String date;
    public String exerciseName;
    public String muscleGroup;
    public int sets;
    public int reps;
    public double weightLbs;
    public int durationMin;
    public int rpe;
    public String notes;

    @Override
    public String toString() {
        return id + "," + date + "," + exerciseName + "," + muscleGroup + "," +
                sets + "," + reps + "," + weightLbs + "," + durationMin + "," + rpe + "," + notes;
    }
}
