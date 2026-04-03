# Booktracker CLI

This project is a simple Java command-line application that uses JDBC with SQLite to manage the Booktracker dataset.

## Requirements

- Java 17
- Maven

## Run

From file path:

```powershell
mvn compile
mvn exec:java
```

## What the program does

- Creates `booktracker.db` if it does not exist
- Creates the `User`, `Book`, and `ReadingHabit` tables
- Adds the `Name` column to the `User` table if it does not exist
- Imports data from `reading_habits_dataset.xlsx` into the database the first time it runs

## Menu options

1. Add a user
2. View all reading habits for a user
3. Update a book title
4. Delete a reading habit
5. Show the mean age of users
6. Count users who read a specific book
7. Show total pages read
8. Count users who read more than one book
9. Exit

## Notes

- The dataset file used by the app is `reading_habits_dataset.xlsx` in the project folder.
- SQL is used for the aggregate calculations such as `AVG`, `SUM`, `GROUP BY`, and `HAVING`.
