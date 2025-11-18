package gym;

// Pedro Salcedo
// CEN 3024 - Phase 4
// Main GUI for my Gym Workout Tracker (SQLite version).
// This handles the program window, input fields, buttons, and table display.

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;

/**
 * Main GUI class that lets the user work with workout sessions.
 * It connects to the database, shows the data in a table,
 * and lets the user add, update, delete, and calculate simple stats.
 */
public class MainGUI extends JFrame {

    private final SessionService service = new SessionService();

    // column setup for the table
    private final String[] cols = {"ID","Date","Exercise","Muscle","Sets","Reps","Weight","Duration","RPE","Notes"};
    private final DefaultTableModel model = new DefaultTableModel(cols, 0);
    private final JTable table = new JTable(model);

    // input fields
    private final JTextField idTxt = new JTextField();
    private final JLabel     idStatus = new JLabel(" ");
    private final JTextField dateTxt = new JTextField();
    private final JTextField exTxt = new JTextField();
    private final JTextField musTxt = new JTextField();
    private final JTextField setsTxt = new JTextField();
    private final JTextField repsTxt = new JTextField();
    private final JTextField wtTxt = new JTextField();
    private final JTextField durTxt = new JTextField();
    private final JTextField rpeTxt = new JTextField();
    private final JTextField notesTxt = new JTextField();

    // DB connection field
    private final JTextField dbPathTxt = new JTextField();

    // main buttons
    private final JButton addBtn    = new JButton("Add");
    private final JButton updateBtn = new JButton("Update");
    private final JButton deleteBtn = new JButton("Delete");
    private final JButton showBtn   = new JButton("Display All");
    private final JButton clearBtn  = new JButton("Clear");
    private final JButton exitBtn   = new JButton("Exit");
    private final JButton customBtn = new JButton("Custom: 1RM + Volume");

    /**
     * Builds the full GUI window.
     */
    public MainGUI() {
        setTitle("Gym Workout Tracker - GUI");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        try { UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel"); } catch (Exception ignore) {}
        setSize(1100, 620);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10,10));
        ((JComponent)getContentPane()).setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        // left panel: data entry and buttons
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.add(title("Session"));
        form.add(row("ID (int):", idTxt));
        form.add(row("ID status:", idStatus));
        form.add(row("Date (yyyy-mm-dd):", dateTxt));
        form.add(row("Exercise:", exTxt));
        form.add(row("Muscle:", musTxt));
        form.add(row("Sets:", setsTxt));
        form.add(row("Reps:", repsTxt));
        form.add(row("Weight (lbs):", wtTxt));
        form.add(row("Duration (min):", durTxt));
        form.add(row("RPE (1-10):", rpeTxt));
        form.add(row("Notes:", notesTxt));
        form.add(Box.createVerticalStrut(6));

        JPanel btns = new JPanel(new GridLayout(3, 2, 8, 8));
        btns.add(addBtn);    btns.add(updateBtn);
        btns.add(deleteBtn); btns.add(showBtn);
        btns.add(clearBtn);  btns.add(exitBtn);

        form.add(btns);
        form.add(Box.createVerticalStrut(6));
        form.setPreferredSize(new Dimension(380, getHeight()));
        idStatus.setForeground(Color.DARK_GRAY);

        // table view on the right
        table.setFillsViewportHeight(true);
        table.setRowHeight(22);
        table.setDefaultEditor(Object.class, null);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel right = new JPanel(new BorderLayout(8,8));
        right.add(new JScrollPane(table), BorderLayout.CENTER);
        right.add(customBtn, BorderLayout.SOUTH);

        // bottom: DB connection bar
        JPanel bottom = new JPanel(new BorderLayout(8,8));
        JButton connectBtn = new JButton("Connect DB");
        bottom.add(new JLabel("DB file path (.db):"), BorderLayout.WEST);
        bottom.add(dbPathTxt, BorderLayout.CENTER);
        bottom.add(connectBtn, BorderLayout.EAST);

        add(form, BorderLayout.WEST);
        add(right, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        // size for text fields
        Dimension fieldSize = new Dimension(220, 28);
        for (JTextField tf : new JTextField[]{idTxt,dateTxt,exTxt,musTxt,setsTxt,repsTxt,wtTxt,durTxt,rpeTxt,notesTxt}) {
            tf.setPreferredSize(fieldSize);
            tf.setMaximumSize(fieldSize);
        }

        // buttons disabled until DB is connected
        setDbButtonsEnabled(false);

        // button listeners
        exitBtn.addActionListener(e -> System.exit(0));

        clearBtn.addActionListener(e -> {
            idTxt.setText(""); dateTxt.setText(""); exTxt.setText(""); musTxt.setText("");
            setsTxt.setText(""); repsTxt.setText(""); wtTxt.setText(""); durTxt.setText("");
            rpeTxt.setText(""); notesTxt.setText(""); idStatus.setText(" ");
            table.clearSelection();
        });

        showBtn.addActionListener(e -> refreshTable());
        connectBtn.addActionListener(e -> onConnectDb());
        addBtn.addActionListener(e -> onAdd());
        deleteBtn.addActionListener(e -> onDelete());
        updateBtn.addActionListener(e -> onUpdate());
        customBtn.addActionListener(e -> onCustom());

        // fill input fields when clicking table rows
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) fillFormFromSelection();
        });
    }

    /**
     * Enables or disables the main DB buttons based on connection status.
     */
    private void setDbButtonsEnabled(boolean on) {
        addBtn.setEnabled(on);
        updateBtn.setEnabled(on);
        deleteBtn.setEnabled(on);
        showBtn.setEnabled(on);
        customBtn.setEnabled(on);
    }

    /**
     * Connects to the SQLite file typed by the user.
     */
    private void onConnectDb() {
        String path = dbPathTxt.getText().trim();
        if (path.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter a .db file path first.");
            return;
        }

        try {
            service.connect(path);
            setDbButtonsEnabled(true);
            refreshTable();
            JOptionPane.showMessageDialog(this, "Connected to database.");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "DB problem", JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Adds a new workout using the values typed on the left.
     */
    private void onAdd() {
        WorkoutSession s;
        int id;
        try {
            id = Integer.parseInt(idTxt.getText().trim());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Please enter a whole number for ID.");
            return;
        }

        if (service.existsId(id)) {
            JOptionPane.showMessageDialog(this, "That ID is already used. Pick a different ID.");
            return;
        }

        try {
            s = readFormUsingId(id);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Problem", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean ok = service.add(s);
        if (!ok) {
            JOptionPane.showMessageDialog(this, "Add failed. Check the values.");
            return;
        }
        refreshTable();
        JOptionPane.showMessageDialog(this, "Added.");
    }

    /**
     * Deletes the selected row.
     */
    private void onDelete() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a row to delete.");
            return;
        }
        int id = (int) model.getValueAt(row, 0);

        int ok = JOptionPane.showConfirmDialog(this, "Delete record " + id + "?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;

        if (!service.deleteById(id)) {
            JOptionPane.showMessageDialog(this, "Delete failed.");
            return;
        }
        refreshTable();
        JOptionPane.showMessageDialog(this, "Deleted.");
    }

    /**
     * Updates a record using the values currently typed on the left.
     */
    private void onUpdate() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a row to update.");
            return;
        }

        int id;
        try {
            id = Integer.parseInt(idTxt.getText().trim());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Please enter a whole number for ID.");
            return;
        }

        if (!service.existsId(id)) {
            JOptionPane.showMessageDialog(this, "That ID does not exist.");
            return;
        }

        WorkoutSession s;
        try {
            s = readFormUsingId(id);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Problem", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean ok = service.updateSession(s);
        if (!ok) {
            JOptionPane.showMessageDialog(this, "Update failed. Check the values.");
            return;
        }
        refreshTable();
        JOptionPane.showMessageDialog(this, "Updated.");
    }

    /**
     * Calculates volume and simple 1RM estimate for the selected row.
     */
    private void onCustom() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a row in the table first.");
            return;
        }

        String date = String.valueOf(model.getValueAt(row, 1));
        String exercise = String.valueOf(model.getValueAt(row, 2));
        int sets = toIntSafe(model.getValueAt(row, 4));
        int reps = toIntSafe(model.getValueAt(row, 5));
        double weight = toDoubleSafe(model.getValueAt(row, 6));

        double volume = sets * reps * weight;
        double best = service.bestE1RMInLast7Days(exercise, date);

        String msg = "Exercise: " + exercise +
                "\nDate used: " + date +
                "\nVolume (sets×reps×weight): " + round1(volume) + " lbs" +
                "\nBest est. 1RM (last 7 days): " + round1(best) + " lbs";
        JOptionPane.showMessageDialog(this, msg);
    }

    /**
     * Loads the selected row into the input fields.
     */
    private void fillFormFromSelection() {
        int row = table.getSelectedRow();
        if (row == -1) {
            idStatus.setText(" ");
            return;
        }
        idTxt.setText(String.valueOf(model.getValueAt(row, 0)));
        dateTxt.setText(String.valueOf(model.getValueAt(row, 1)));
        exTxt.setText(String.valueOf(model.getValueAt(row, 2)));
        musTxt.setText(String.valueOf(model.getValueAt(row, 3)));
        setsTxt.setText(String.valueOf(model.getValueAt(row, 4)));
        repsTxt.setText(String.valueOf(model.getValueAt(row, 5)));
        wtTxt.setText(String.valueOf(model.getValueAt(row, 6)));
        durTxt.setText(String.valueOf(model.getValueAt(row, 7)));
        rpeTxt.setText(String.valueOf(model.getValueAt(row, 8)));
        Object notes = model.getValueAt(row, 9);
        notesTxt.setText(notes == null ? "" : String.valueOf(notes));
        idStatus.setText("row selected");
    }

    /**
     * Reads all form values and builds a WorkoutSession object.
     */
    private WorkoutSession readFormUsingId(int id) {
        WorkoutSession s = new WorkoutSession();
        s.id = id;

        String d = dateTxt.getText().trim();
        if (!d.matches("\\d{4}-\\d{2}-\\d{2}"))
            throw new IllegalArgumentException("Please enter a date (e.g., 2025-10-27).");
        s.date = d;

        s.exerciseName = mustText(exTxt.getText(), "Exercise");
        s.muscleGroup  = mustText(musTxt.getText(), "Muscle");
        s.sets         = parseIntNice(setsTxt.getText(), "Sets");
        s.reps         = parseIntNice(repsTxt.getText(), "Reps");
        s.weightLbs    = parseDoubleNice(wtTxt.getText(), "Weight");
        s.durationMin  = parseIntNice(durTxt.getText(), "Duration");
        s.rpe          = parseIntNice(rpeTxt.getText(), "RPE");
        if (s.rpe < 1 || s.rpe > 10) throw new IllegalArgumentException("RPE must be between 1 and 10.");
        s.notes        = notesTxt.getText().trim();
        return s;
    }

    /**
     * Refreshes the table with the latest data from the DB.
     */
    private void refreshTable() {
        ArrayList<WorkoutSession> rows = service.listAll();
        model.setRowCount(0);
        for (WorkoutSession s : rows) {
            model.addRow(new Object[]{
                    s.id, s.date, s.exerciseName, s.muscleGroup,
                    s.sets, s.reps, s.weightLbs, s.durationMin, s.rpe, s.notes
            });
        }
    }

    // basic parsing helpers
    private int parseIntNice(String s, String label) {
        try { return Integer.parseInt(s.trim()); }
        catch (Exception e) { throw new IllegalArgumentException(label + " must be a whole number."); }
    }
    private double parseDoubleNice(String s, String label) {
        try { return Double.parseDouble(s.trim()); }
        catch (Exception e) { throw new IllegalArgumentException(label + " must be a number."); }
    }
    private String mustText(String s, String label) {
        String t = s.trim();
        if (t.isEmpty()) throw new IllegalArgumentException("Please enter " + label.toLowerCase() + ".");
        return t;
    }
    private int toIntSafe(Object o) {
        try { return Integer.parseInt(String.valueOf(o)); } catch (Exception e) { return 0; }
    }
    private double toDoubleSafe(Object o) {
        try { return Double.parseDouble(String.valueOf(o)); } catch (Exception e) { return 0.0; }
    }
    private String round1(double x) {
        return String.format("%.1f", x);
    }

    // small UI helpers
    private JPanel row(String label, JComponent comp) {
        JPanel p = new JPanel(new BorderLayout(8,0));
        JLabel l = new JLabel(label);
        l.setPreferredSize(new Dimension(140, 28));
        p.add(l, BorderLayout.WEST);
        p.add(comp, BorderLayout.CENTER);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        return p;
    }
    private JLabel title(String t) {
        JLabel l = new JLabel(t);
        l.setFont(l.getFont().deriveFont(Font.BOLD, 16f));
        l.setBorder(BorderFactory.createEmptyBorder(0,0,8,0));
        return l;
    }

    /** Starts the program and opens the window. */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainGUI().setVisible(true));
    }
}
