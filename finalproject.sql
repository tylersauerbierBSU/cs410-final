-- Name: FinalProject
-- Author: Anton Leslie, Andrew Martinez, Tyler Sauerbier
-- Date: 2024-04-18
-- Description: This script creates the 'finalproject' database and sets up the schema
-- for managing courses, categories, assignments, students, enrollments, and grades.

-- Create the 'finalproject' database
CREATE DATABASE finalproject;

-- Select the 'finalproject' database for use
USE finalproject;

-- Create 'Courses' table to store course details
CREATE TABLE Courses (
    CourseID INT AUTO_INCREMENT PRIMARY KEY,
    CourseNumber VARCHAR(10) NOT NULL,
    Term VARCHAR(10) NOT NULL,
    SectionNumber INT NOT NULL,
    Description TEXT,
    -- Ensures that each course is uniquely identified by a combination of its number, term, and section
    UNIQUE (CourseNumber, Term, SectionNumber)
);

-- Create 'Categories' table to store categories of assessments within courses
CREATE TABLE Categories (
    CategoryID INT AUTO_INCREMENT PRIMARY KEY,
    CourseID INT NOT NULL,
    Name VARCHAR(50) NOT NULL,
    Weight DECIMAL(5,2) NOT NULL,
    -- Establishes a foreign key relationship with the Courses table
    FOREIGN KEY (CourseID) REFERENCES Courses(CourseID),
    -- Ensures category names are unique within the same course
    UNIQUE (CourseID, Name)
);

-- Create 'Assignments' table to store assignments within categories
CREATE TABLE Assignments (
    AssignmentID INT AUTO_INCREMENT PRIMARY KEY,
    CategoryID INT NOT NULL,
    Name VARCHAR(255) NOT NULL,
    Description TEXT,
    Points INT NOT NULL,
    -- Establishes a foreign key relationship with the Categories table
    FOREIGN KEY (CategoryID) REFERENCES Categories(CategoryID),
    -- Ensures assignment names are unique within the same category
    UNIQUE (CategoryID, Name)
);

-- Create 'Students' table to store student information
CREATE TABLE Students (
    StudentID INT AUTO_INCREMENT PRIMARY KEY,
    Username VARCHAR(100) NOT NULL UNIQUE,
    Name VARCHAR(100) NOT NULL
);

-- Create 'Enrollments' table to record which students are enrolled in which courses
CREATE TABLE Enrollments (
    StudentID INT NOT NULL,
    CourseID INT NOT NULL,
    -- Establishes foreign key relationships with the Students and Courses tables
    FOREIGN KEY (StudentID) REFERENCES Students(StudentID),
    FOREIGN KEY (CourseID) REFERENCES Courses(CourseID),
    -- Composite key to ensure unique enrollment pairs
    PRIMARY KEY (StudentID, CourseID)
);

-- Create 'Grades' table to store grades that students receive for assignments
CREATE TABLE Grades (
    StudentID INT NOT NULL,
    AssignmentID INT NOT NULL,
    Grade DECIMAL(5,2) NOT NULL,
    -- Establishes foreign key relationships with the Students and Assignments tables
    FOREIGN KEY (StudentID) REFERENCES Students(StudentID),
    FOREIGN KEY (AssignmentID) REFERENCES Assignments(AssignmentID),
    -- Composite key to ensure unique grade entries per assignment
    PRIMARY KEY (StudentID, AssignmentID)
);
