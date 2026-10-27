package com.examresults;

import java.sql.*;
import java.util.Scanner;

/**
 * Examination Result Management System
 * Java JDBC application connected to a MySQL database.
 *
 * PASS rule: a student must score at least 40 in EACH subject.
 */
public class Main {
    private static final Scanner INPUT = new Scanner(System.in);
    private static final int PASS_MARK = 40;

    // Set these environment variables if your local MySQL settings differ.
    private static final String DB_URL = System.getenv().getOrDefault(
            "EXAM_DB_URL",
            "jdbc:mysql://localhost:3306/exam_results?serverTimezone=UTC");
    private static final String DB_USER = System.getenv().getOrDefault("EXAM_DB_USER", "root");
    private static final String DB_PASSWORD = System.getenv().getOrDefault("EXAM_DB_PASSWORD", "");

    public static void main(String[] args) {
        System.out.println("==========================================");
        System.out.println(" EXAMINATION RESULT MANAGEMENT SYSTEM");
        System.out.println("            Java + JDBC + MySQL");
        System.out.println("==========================================");

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            System.out.println("Connected to MySQL successfully.");
            runMenu(connection);
        } catch (SQLException e) {
            System.err.println("\nDatabase connection failed: " + e.getMessage());
            System.err.println("Check that MySQL Server is running, database/schema.sql was executed,");
            System.err.println("and your EXAM_DB_USER / EXAM_DB_PASSWORD settings are correct.");
        }
    }

    private static void runMenu(Connection connection) {
        while (true) {
            System.out.println("\n--------------- MENU ---------------");
            System.out.println("1. Add student result");
            System.out.println("2. View all students");
            System.out.println("3. Search/display one student's result");
            System.out.println("4. List passed students");
            System.out.println("5. List failed students");
            System.out.println("6. Update student result");
            System.out.println("7. Delete student");
            System.out.println("8. Dashboard summary");
            System.out.println("0. Exit");

            int choice = readInt("Choose an option: ", 0, 8);
            try {
                switch (choice) {
                    case 1 -> addStudent(connection);
                    case 2 -> listStudents(connection, "ALL");
                    case 3 -> showOneStudent(connection);
                    case 4 -> listStudents(connection, "PASS");
                    case 5 -> listStudents(connection, "FAIL");
                    case 6 -> updateStudent(connection);
                    case 7 -> deleteStudent(connection);
                    case 8 -> showDashboard(connection);
                    case 0 -> {
                        System.out.println("Goodbye!");
                        return;
                    }
                    default -> System.out.println("Invalid option.");
                }
            } catch (SQLException e) {
                System.out.println("Database operation failed: " + e.getMessage());
            }
        }
    }

    private static void addStudent(Connection connection) throws SQLException {
        String roll = readText("Roll number: ", 30);
        String name = readText("Student name: ", 100);
        int m1 = readInt("Subject 1 marks (0-100): ", 0, 100);
        int m2 = readInt("Subject 2 marks (0-100): ", 0, 100);
        int m3 = readInt("Subject 3 marks (0-100): ", 0, 100);

        String sql = """
                INSERT INTO students (roll_number, full_name, subject1, subject2, subject3)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, roll);
            ps.setString(2, name);
            ps.setInt(3, m1);
            ps.setInt(4, m2);
            ps.setInt(5, m3);
            ps.executeUpdate();
            System.out.println("Student result saved successfully.");
            printResult(roll, name, m1, m2, m3);
        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("That roll number already exists. Please use a unique roll number.");
        }
    }

    private static void listStudents(Connection connection, String filter) throws SQLException {
        String condition = switch (filter) {
            case "PASS" -> " WHERE subject1 >= 40 AND subject2 >= 40 AND subject3 >= 40";
            case "FAIL" -> " WHERE subject1 < 40 OR subject2 < 40 OR subject3 < 40";
            default -> "";
        };
        String sql = "SELECT roll_number, full_name, subject1, subject2, subject3 FROM students"
                + condition + " ORDER BY roll_number";

        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            System.out.println("\n" + (filter.equals("ALL") ? "ALL STUDENTS" :
                    filter.equals("PASS") ? "PASSED STUDENTS" : "FAILED STUDENTS"));
            printTableHeader();
            boolean found = false;
            while (rs.next()) {
                printTableRow(rs);
                found = true;
            }
            if (!found) System.out.println("No students found.");
        }
    }

    private static void showOneStudent(Connection connection) throws SQLException {
        String roll = readText("Enter roll number: ", 30);
        String sql = "SELECT roll_number, full_name, subject1, subject2, subject3 FROM students WHERE roll_number = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, roll);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("Student not found.");
                    return;
                }
                System.out.println("\n--- STUDENT RESULT ---");
                printResult(rs.getString("roll_number"), rs.getString("full_name"),
                        rs.getInt("subject1"), rs.getInt("subject2"), rs.getInt("subject3"));
            }
        }
    }

    private static void updateStudent(Connection connection) throws SQLException {
        String roll = readText("Roll number to update: ", 30);
        String name = readText("New student name: ", 100);
        int m1 = readInt("New subject 1 marks (0-100): ", 0, 100);
        int m2 = readInt("New subject 2 marks (0-100): ", 0, 100);
        int m3 = readInt("New subject 3 marks (0-100): ", 0, 100);

        String sql = "UPDATE students SET full_name=?, subject1=?, subject2=?, subject3=? WHERE roll_number=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setInt(2, m1);
            ps.setInt(3, m2);
            ps.setInt(4, m3);
            ps.setString(5, roll);
            if (ps.executeUpdate() == 1) {
                System.out.println("Student result updated.");
                printResult(roll, name, m1, m2, m3);
            } else {
                System.out.println("Student not found.");
            }
        }
    }

    private static void deleteStudent(Connection connection) throws SQLException {
        String roll = readText("Roll number to delete: ", 30);
        String confirmation = readText("Type DELETE to confirm: ", 10);
        if (!confirmation.equals("DELETE")) {
            System.out.println("Delete cancelled.");
            return;
        }
        try (PreparedStatement ps = connection.prepareStatement(
                "DELETE FROM students WHERE roll_number = ?")) {
            ps.setString(1, roll);
            System.out.println(ps.executeUpdate() == 1 ? "Student deleted." : "Student not found.");
        }
    }

    private static void showDashboard(Connection connection) throws SQLException {
        String sql = """
                SELECT COUNT(*) AS total,
                       COALESCE(SUM(subject1 >= 40 AND subject2 >= 40 AND subject3 >= 40), 0) AS passed
                FROM students
                """;
        try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            rs.next();
            int total = rs.getInt("total");
            int passed = rs.getInt("passed");
            int failed = total - passed;
            double rate = total == 0 ? 0.0 : passed * 100.0 / total;
            System.out.println("\n--------- DASHBOARD ---------");
            System.out.println("Total students: " + total);
            System.out.println("Passed: " + passed);
            System.out.println("Failed: " + failed);
            System.out.printf("Pass rate: %.2f%%%n", rate);
        }
    }

    private static void printTableHeader() {
        System.out.printf("%-14s %-24s %-8s %-8s %-8s %-8s %-8s%n",
                "ROLL NUMBER", "NAME", "SUB 1", "SUB 2", "SUB 3", "TOTAL", "RESULT");
        System.out.println("-".repeat(82));
    }

    private static void printTableRow(ResultSet rs) throws SQLException {
        int m1 = rs.getInt("subject1");
        int m2 = rs.getInt("subject2");
        int m3 = rs.getInt("subject3");
        System.out.printf("%-14s %-24s %-8d %-8d %-8d %-8d %-8s%n",
                rs.getString("roll_number"), rs.getString("full_name"),
                m1, m2, m3, m1 + m2 + m3, isPass(m1, m2, m3) ? "PASS" : "FAIL");
    }

    private static void printResult(String roll, String name, int m1, int m2, int m3) {
        int total = m1 + m2 + m3;
        double percentage = total / 3.0;
        System.out.println("Roll number : " + roll);
        System.out.println("Name        : " + name);
        System.out.println("Subject 1   : " + m1);
        System.out.println("Subject 2   : " + m2);
        System.out.println("Subject 3   : " + m3);
        System.out.println("Total       : " + total + "/300");
        System.out.printf("Percentage  : %.2f%%%n", percentage);
        System.out.println("Result      : " + (isPass(m1, m2, m3) ? "PASS" : "FAIL"));
    }

    private static boolean isPass(int m1, int m2, int m3) {
        return m1 >= PASS_MARK && m2 >= PASS_MARK && m3 >= PASS_MARK;
    }

    private static int readInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            try {
                int value = Integer.parseInt(INPUT.nextLine().trim());
                if (value < min || value > max) {
                    System.out.println("Enter a number between " + min + " and " + max + ".");
                } else return value;
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid whole number.");
            }
        }
    }

    private static String readText(String prompt, int maxLength) {
        while (true) {
            System.out.print(prompt);
            String value = INPUT.nextLine().trim();
            if (value.isEmpty()) System.out.println("This field cannot be empty.");
            else if (value.length() > maxLength) System.out.println("Maximum length is " + maxLength + " characters.");
            else return value;
        }
    }
}
