import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class QuestionEntryGUI extends JFrame implements ActionListener {
    private final JTextField questionField;
    private final JTextField option1Field;
    private final JTextField option2Field;
    private final JTextField option3Field;
    private final JTextField option4Field;
    private final JRadioButton correctOption1RadioButton;
    private final JRadioButton correctOption2RadioButton;
    private final JRadioButton correctOption3RadioButton;
    private final JRadioButton correctOption4RadioButton;
    private final JButton submitButton;
    private final JButton removeAllButton;

    // JDBC variables
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/quizdb";
    private static final String JDBC_USER = "omj";
    private static final String JDBC_PASSWORD = "2935";

    public QuestionEntryGUI() {
        questionField = new JTextField();
        option1Field = new JTextField();
        option2Field = new JTextField();
        option3Field = new JTextField();
        option4Field = new JTextField();

        correctOption1RadioButton = new JRadioButton("Option 1");
        correctOption2RadioButton = new JRadioButton("Option 2");
        correctOption3RadioButton = new JRadioButton("Option 3");
        correctOption4RadioButton = new JRadioButton("Option 4");

        ButtonGroup correctOptionGroup = new ButtonGroup();
        correctOptionGroup.add(correctOption1RadioButton);
        correctOptionGroup.add(correctOption2RadioButton);
        correctOptionGroup.add(correctOption3RadioButton);
        correctOptionGroup.add(correctOption4RadioButton);

        submitButton = new JButton("Submit");
        removeAllButton = new JButton("Remove All");

        submitButton.addActionListener(this);
        removeAllButton.addActionListener(this);

        setLayout(new GridLayout(9, 2, 10, 10));
        add(new JLabel("Question:"));
        add(questionField);
        add(new JLabel("Options:"));
        add(option1Field);
        add(correctOption1RadioButton);
        add(option2Field);
        add(correctOption2RadioButton);
        add(option3Field);
        add(correctOption3RadioButton);
        add(option4Field);
        add(correctOption4RadioButton);
        add(new JLabel(""));
        add(submitButton);
        add(removeAllButton);

        setTitle("Question Entry");
        setSize(400, 400);
        setLocationRelativeTo(null);  // Center on screen
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(QuestionEntryGUI::new);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == submitButton) {
            insertQuestionIntoDatabase();
        } else if (e.getSource() == removeAllButton) {
            removeAllQuestionsFromDatabase();
        }
    }

    private void insertQuestionIntoDatabase() {
        String questionText = questionField.getText();
        String option1Text = option1Field.getText();
        String option2Text = option2Field.getText();
        String option3Text = option3Field.getText();
        String option4Text = option4Field.getText();
        String correctOption = getSelectedCorrectOption();

        try (Connection connection = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD)) {
            // Modified query to exclude the 'id' column
            String query = "INSERT INTO questions (question, option1, option2, option3, option4, correct_option) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, questionText);
                preparedStatement.setString(2, option1Text);
                preparedStatement.setString(3, option2Text);
                preparedStatement.setString(4, option3Text);
                preparedStatement.setString(5, option4Text);
                preparedStatement.setString(6, correctOption);

                int rowsAffected = preparedStatement.executeUpdate();
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Question inserted successfully!");
                    clearFields();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to insert question.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database connection error.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String getSelectedCorrectOption() {
        if (correctOption1RadioButton.isSelected()) {
            return option1Field.getText();
        } else if (correctOption2RadioButton.isSelected()) {
            return option2Field.getText();
        } else if (correctOption3RadioButton.isSelected()) {
            return option3Field.getText();
        } else if (correctOption4RadioButton.isSelected()) {
            return option4Field.getText();
        }
        return "";
    }

    private void removeAllQuestionsFromDatabase() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to remove all questions from the database?",
                "Confirm Remove All",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection connection = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD)) {
                String query = "DELETE FROM questions";
                try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                    int rowsAffected = preparedStatement.executeUpdate();
                    if (rowsAffected > 0) {
                        // Resetting the auto-increment index
                        resetAutoIncrement();
                        JOptionPane.showMessageDialog(this, "All questions removed from the database.");
                    } else {
                        JOptionPane.showMessageDialog(this, "No questions to remove.", "Info", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Database connection error.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearFields() {
        questionField.setText("");
        option1Field.setText("");
        option2Field.setText("");
        option3Field.setText("");
        option4Field.setText("");
        correctOption1RadioButton.setSelected(false);
        correctOption2RadioButton.setSelected(false);
        correctOption3RadioButton.setSelected(false);
        correctOption4RadioButton.setSelected(false);
    }

    // Method to reset the auto-increment index
    private void resetAutoIncrement() {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD)) {
            String query = "ALTER TABLE questions AUTO_INCREMENT = 1";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.executeUpdate();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database connection error.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
