-- Inserting dummy data into the 'Courses' table
INSERT INTO Courses (CourseNumber, Term, SectionNumber, Description) VALUES
('CSCI101', 'Fall2024', 1, 'Introduction to Computer Science'),
('MATH201', 'Spring2024', 1, 'Calculus I'),
('ENGL102', 'Fall2024', 2, 'English Composition II');

-- Inserting dummy data into the 'Categories' table
INSERT INTO Categories (CourseID, Name, Weight) VALUES
(1, 'Homework', 25.00),
(1, 'Exams', 75.00),
(2, 'Quizzes', 30.00),
(2, 'Final', 70.00);

-- Inserting dummy data into the 'Assignments' table
INSERT INTO Assignments (CategoryID, Name, Description, Points) VALUES
(1, 'HW1', 'First homework on basic concepts', 100),
(1, 'HW2', 'Second homework on loops and conditions', 100),
(2, 'Midterm', 'Midterm covering all topics', 200),
(4, 'Calculus Final', 'Comprehensive final exam', 300);

-- Inserting dummy data into the 'Students' table
INSERT INTO Students (Username, Name) VALUES
('john_doe', 'John Doe'),
('jane_smith', 'Jane Smith'),
('alice_johnson', 'Alice Johnson');

-- Inserting dummy data into the 'Enrollments' table
INSERT INTO Enrollments (StudentID, CourseID) VALUES
(1, 1),
(2, 1),
(3, 2);

-- Inserting dummy data into the 'Grades' table
INSERT INTO Grades (StudentID, AssignmentID, Grade) VALUES
(1, 1, 85.00),
(1, 2, 90.00),
(1, 3, 88.00),
(2, 1, 92.00),
(3, 4, 78.00);


