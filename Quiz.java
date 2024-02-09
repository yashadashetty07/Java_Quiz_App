import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import java.sql.*;

public class Quiz extends JFrame implements ActionListener {
    private Question[] questions;
    private final JLabel label;
    private final JRadioButton[] radioButtons;
    private final JButton btnNext;
    private final JButton btnPrevious;
    private final JButton btnResult;
    private int count = 0;
    private int current = 0;
    private final ButtonGroup buttonGroup;

    // JDBC variables
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/quizdb";
    private static final String JDBC_USER = "omj";
    private static final String JDBC_PASSWORD = "2935";
    private int NUM_QUESTIONS;

    public Quiz() {
        label = new JLabel();
        add(label);

        buttonGroup = new ButtonGroup();
        radioButtons = new JRadioButton[4];
        for (int i = 0; i < 4; i++) {
            radioButtons[i] = new JRadioButton();
            buttonGroup.add(radioButtons[i]);
            add(radioButtons[i]);
        }

        btnNext = new JButton("Next");
        btnPrevious = new JButton("Previous");
        btnResult = new JButton("Result");
        btnResult.setVisible(false);
        btnNext.addActionListener(this);
        btnPrevious.addActionListener(this);
        btnResult.addActionListener(this);
        add(btnNext);
        add(btnPrevious);
        add(btnResult);

        setLayout(null);
        label.setBounds(30, 40, 450, 20);
        radioButtons[0].setBounds(50, 80, 450, 20);
        radioButtons[1].setBounds(50, 110, 200, 20);
        radioButtons[2].setBounds(50, 140, 200, 20);
        radioButtons[3].setBounds(50, 170, 200, 20);
        btnPrevious.setBounds(50, 240, 100, 30);
        btnNext.setBounds(160, 240, 100, 30);
        btnResult.setBounds(270, 240, 100, 30);
    }

    public void start() {
        new Thread(() -> {
            loadQuestionsFromDatabase();
            SwingUtilities.invokeLater(this::set);
        }).start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Quiz quiz = new Quiz();
            quiz.setTitle("Simple Quiz App");
            quiz.setSize(600, 350);
            quiz.setLocation(250, 100);
            quiz.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            quiz.setVisible(true);
            quiz.start();
        });
    }

    private void loadQuestionsFromDatabase() {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD)) {
            String countQuery = "SELECT COUNT(*) FROM questions";
            try (PreparedStatement countStatement = connection.prepareStatement(countQuery)) {
                try (ResultSet countResultSet = countStatement.executeQuery()) {
                    if (countResultSet.next()) {
                        NUM_QUESTIONS = countResultSet.getInt(1);
                    }
                }
            }

            questions = new Question[NUM_QUESTIONS];
            String query = "SELECT * FROM questions";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    int i = 0;
                    while (resultSet.next() && i < NUM_QUESTIONS) {
                        int id = resultSet.getInt("id");
                        String questionText = resultSet.getString("question");
                        String option1 = resultSet.getString("option1");
                        String option2 = resultSet.getString("option2");
                        String option3 = resultSet.getString("option3");
                        String option4 = resultSet.getString("option4");
                        String correctOption = resultSet.getString("correct_option");

                        Question question = new Question(id, questionText, option1, option2, option3, option4, correctOption);
                        questions[i] = question;
                        i++;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void set() {
        if (current >= 0 && current < NUM_QUESTIONS) {
            Question currentQuestion = questions[current];
            label.setText("Que" + (currentQuestion.getId()) + ": " + currentQuestion.getQuestionText());

            resetRadioButtons();  // Reset all radio buttons to unselected

            radioButtons[0].setText(currentQuestion.getOption1());
            radioButtons[1].setText(currentQuestion.getOption2());
            radioButtons[2].setText(currentQuestion.getOption3());
            radioButtons[3].setText(currentQuestion.getOption4());

            String selectedAnswer = currentQuestion.getSelectedAnswer();
            if (selectedAnswer != null) {
                for (JRadioButton radioButton : radioButtons) {
                    if (selectedAnswer.equals(radioButton.getText())) {
                        radioButton.setSelected(true);
                        break;
                    }
                }
            }
        }
    }

    private void resetRadioButtons() {
        buttonGroup.clearSelection();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnNext) {
            if (check())
                count = count + 1;
            current++;
            SwingUtilities.invokeLater(this::set);
            if (current == NUM_QUESTIONS - 1) {
                btnNext.setEnabled(false);
                btnResult.setVisible(true);
                btnResult.setText("Result");
            }
        } else if (e.getSource() == btnPrevious) {
            current--;
            if (current < 0) {
                current = 0;
            }
            SwingUtilities.invokeLater(this::set);
        } else if (e.getActionCommand().equals("Result")) {
            if (check())
                count = count + 1;
            current++;
            JOptionPane.showMessageDialog(this, "Correct answers= " + count);
            System.exit(0);
        }
    }

    private boolean check() {
        if (current >= 0 && current < NUM_QUESTIONS) {
            String selectedAnswer = getSelectedAnswer();
            questions[current].setSelectedAnswer(selectedAnswer);  // Save the selected answer
            String correctAnswer = questions[current].getCorrectOption();
            return selectedAnswer != null && selectedAnswer.equals(correctAnswer);
        }
        return false;
    }

    private String getSelectedAnswer() {
        for (JRadioButton radioButton : radioButtons) {
            if (radioButton.isSelected()) {
                return radioButton.getText();
            }
        }
        return null;
    }
}

class Question {
    private final int id;
    private final String questionText;
    private final String option1;
    private final String option2;
    private final String option3;
    private final String option4;
    private final String correctOption;
    private String selectedAnswer;  // Newly added field to store the selected answer

    public Question(int id, String questionText, String option1, String option2, String option3, String option4, String correctOption) {
        this.id = id;
        this.questionText = questionText;
        this.option1 = option1;
        this.option2 = option2;
        this.option3 = option3;
        this.option4 = option4;
        this.correctOption = correctOption;
    }

    public int getId() {
        return id;
    }

    public String getQuestionText() {
        return questionText;
    }

    public String getOption1() {
        return option1;
    }

    public String getOption2() {
        return option2;
    }

    public String getOption3() {
        return option3;
    }

    public String getOption4() {
        return option4;
    }

    public String getCorrectOption() {
        return correctOption;
    }

    public String getSelectedAnswer() {
        return selectedAnswer;
    }

    public void setSelectedAnswer(String selectedAnswer) {
        this.selectedAnswer = selectedAnswer;
    }
}
