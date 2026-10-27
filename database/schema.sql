-- Examination Result Management System
-- Run this entire file in MySQL Workbench.

CREATE DATABASE IF NOT EXISTS exam_results
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE exam_results;

CREATE TABLE IF NOT EXISTS students (
    id INT AUTO_INCREMENT PRIMARY KEY,
    roll_number VARCHAR(30) NOT NULL UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    subject1 INT NOT NULL,
    subject2 INT NOT NULL,
    subject3 INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_subject1 CHECK (subject1 BETWEEN 0 AND 100),
    CONSTRAINT chk_subject2 CHECK (subject2 BETWEEN 0 AND 100),
    CONSTRAINT chk_subject3 CHECK (subject3 BETWEEN 0 AND 100)
);

-- Optional test data. Uncomment the INSERT statement to use it.
-- INSERT INTO students (roll_number, full_name, subject1, subject2, subject3)
-- VALUES ('101', 'Rahul Kumar', 75, 80, 65),
--        ('102', 'Priya Sharma', 35, 70, 60);

-- Quick report: all students and calculated pass/fail status.
SELECT
    roll_number,
    full_name,
    subject1,
    subject2,
    subject3,
    (subject1 + subject2 + subject3) AS total_marks,
    ROUND((subject1 + subject2 + subject3) / 3.0, 2) AS percentage,
    CASE
        WHEN subject1 >= 40 AND subject2 >= 40 AND subject3 >= 40 THEN 'PASS'
        ELSE 'FAIL'
    END AS result
FROM students
ORDER BY roll_number;

-- Passed students:
-- SELECT * FROM students
-- WHERE subject1 >= 40 AND subject2 >= 40 AND subject3 >= 40;

-- Failed students:
-- SELECT * FROM students
-- WHERE subject1 < 40 OR subject2 < 40 OR subject3 < 40;
