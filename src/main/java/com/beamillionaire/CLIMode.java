package com.beamillionaire;

import com.beamillionaire.domain.*;
import com.beamillionaire.engine.QuestionSelector;
import java.util.*;
import java.nio.file.Path;
import com.beamillionaire.storage.AppPaths;
import java.io.IOException;
import com.beamillionaire.storage.CsvQuestionRepository;

/** Temporary practice screen using the real PR 5 selector and models. */
public class CLIMode {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        Path directory = AppPaths.questionDataDirectory().toAbsolutePath();
        System.out.println("QUESTION BANK PRACTICE");
        System.out.println("File: " + directory.resolve("questions.csv"));

        while (true) {
            QuestionBank bank;
            try {
                bank = new CsvQuestionRepository(directory).load();
            } catch (IOException error) {
                System.out.println("Could not load questions:");
                System.out.println(error.getMessage());
                System.exit(1);
                return;
            }
            System.out.println();
            System.out.println("Loaded " + bank.size() + " questions.");
            List<Category> available = new ArrayList<>();
            for (Category category : Category.values()) {
                int easy = bank.pool(category, Difficulty.EASY).size();
                int medium = bank.pool(category, Difficulty.MEDIUM).size();
                int hard = bank.pool(category, Difficulty.HARD).size();
                if (easy + medium + hard == 0) continue;
                System.out.println(category.displayName() + ": " + easy + " easy, " + medium + " medium, " + hard + " hard");
                if (easy >= 5 && medium >= 5 && hard >= 5) available.add(category);
                else System.out.println("  Not ready: needs at least 5 questions per difficulty.");
            }
            System.out.println();
            System.out.println("Choose a category");
            for (int i = 0; i < available.size(); i++) {
                System.out.println("  " + (i + 1) + ". " + available.get(i).displayName());
            }
            if (available.isEmpty()) System.out.println("  No playable categories yet.");
            System.out.println("  R. Reload CSV");
            System.out.println("  Q. Quit");
            System.out.print("Your choice: ");
            String choice = read(input);
            if (choice.equals("Q")) return;
            if (choice.equals("R")) continue;
            int number;
            try {
                number = Integer.parseInt(choice);
            } catch (NumberFormatException error) {
                System.out.println("Enter a category number, R or Q.");
                continue;
            }
            if (number < 1 || number > available.size()) {
                System.out.println("Choose one of the listed categories.");
                continue;
            }
            Category category = available.get(number - 1);
            List<Question> questions = new QuestionSelector().select(bank, category, new Random());
            int score = 0;
            int answered = 0;
            boolean backToMenu = false;

            for (Question question : questions) {
                System.out.println();
                System.out.println("----------------------------------------");
                System.out.printf("QUESTION %d OF %d  -  %s%n%n", answered + 1, questions.size(), question.difficulty());
                System.out.println(question.text());
                System.out.println();
                for (Choice option : question.choices()) {
                    System.out.println("  " + option.id() + ") " + option.text());
                }
                System.out.println();
                System.out.println("H: Hint   I: Inspect model   M: Menu   Q: Quit");

                while (true) {
                    System.out.print("Your answer (A-D): ");
                    choice = read(input);
                    if (choice.equals("Q")) return;
                    if (choice.equals("M")) { backToMenu = true; break; }
                    if (choice.equals("H")) {
                        System.out.println("Hint: " + question.clue());
                        continue;
                    }
                    if (choice.equals("I")) {
                        showModel(question);
                        continue;
                    }
                    if (!choice.matches("[A-D]")) {
                        System.out.println("Please enter A-D, H, I, M or Q.");
                        continue;
                    }

                    answered++;
                    System.out.println();
                    if (choice.equals(question.correctChoiceId())) {
                        score++;
                        System.out.println("Correct!");
                    } else {
                        for (Choice option : question.choices()) {
                            if (option.id().equals(question.correctChoiceId())) {
                                System.out.println("Not quite. The answer is " + option.id() + ") " + option.text());
                            }
                        }
                    }
                    System.out.printf("Score: %d / %d%n", score, answered);
                    break;
                }
                if (backToMenu) break;
                if (answered < questions.size()) {
                    System.out.print("Press Enter for the next question, or Q to quit: ");
                    if (read(input).equals("Q")) return;
                }
            }
            System.out.printf("%nPractice finished. Score: %d / %d%n", score, answered);
        }
    }

    private static String read(Scanner input) {
        if (!input.hasNextLine()) return "Q";
        return input.nextLine().trim().toUpperCase(Locale.ROOT);
    }

    private static void showModel(Question question) {
        System.out.println();
        System.out.println("MODEL DETAILS (reveals the answer)");
        System.out.println("  ID: " + question.id());
        System.out.println("  Category: " + question.category().displayName());
        System.out.println("  Difficulty: " + question.difficulty());
        System.out.println("  Text: " + question.text());
        for (Choice option : question.choices()) {
            System.out.println("  Choice " + option.id() + ": " + option.text());
        }
        System.out.println("  Correct choice: " + question.correctChoiceId());
        System.out.println("  Clue: " + question.clue());
        System.out.println();
    }

}
