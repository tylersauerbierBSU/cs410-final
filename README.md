# cs410-final


## Authors
- Anton Leslie
- Andrew Martinez
- Tyler Sauerbier

## Description
GradeManagerShell is a simple command-line interface for managing grades and students in a database. The program provides a shell interface to interact with a database containing information about classes, assignments, categories, and students.

## Describing your implementation:

DatabaseSetup Class:

Establishes a connection to the database using JDBC (Java Database Connectivity).

The getDatabaseConnection method takes the port number and password as parameters to connect to the MySQL database.

GradeManagerShell Class:

Acts as a command-line interface for managing grades.

Utilizes the connection established in DatabaseSetup.

Parses user commands and executes corresponding actions.

Commands include creating a new class, listing classes, selecting a class, showing class details, managing categories and assignments, managing students, and grading.

Methods:
connectToDatabase: Establishes a connection to the database.
closeConnection: Closes the database connection.
interpretCommand: Interprets user commands and executes corresponding actions.

Methods for managing classes: createClass, listClasses, selectClass, showClass.

Methods for managing categories and assignments: showCategories, addCategory, showAssignments, addAssignment.

Methods for managing students: addStudent, showStudents.

Methods for grading: assignGrade, showStudentGrades, showGradebook.

Error Handling:

SQLExceptions are caught and handled appropriately with error messages.
Command-Line Interface:

Provides a simple command-line interface for users to interact with the grade management system.

Users can input commands to perform various actions such as creating classes, adding assignments, enrolling students, and grading.

SQL Queries:

SQL queries are used to interact with the database, including retrieving data and updating records.

Overall, this code provides a framework for managing grades, classes, assignments, and students within a relational database using Java. It follows best practices for database connectivity and error handling.
