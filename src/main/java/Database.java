import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Database {
    private static final String URL = "jdbc:sqlite:booktracker.db";
    private static final String DATASET_PATH = "reading_habits_dataset.xlsx";

    public Database() {
    }

    public Connection connect() {
        try {
            Connection connection = DriverManager.getConnection(URL);
            System.out.println("Connected to SQLite database.");
            return connection;
        } catch (SQLException e) {
            System.out.println("Could not connect to database: " + e.getMessage());
            return null;
        }
    }

    public void createTables() {
        String createUserTable = """
                CREATE TABLE IF NOT EXISTS User (
                    id INTEGER PRIMARY KEY,
                    age INTEGER,
                    gender TEXT
                )
                """;

        String createBookTable = """
                CREATE TABLE IF NOT EXISTS Book (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    title TEXT UNIQUE
                )
                """;

        String createReadingHabitTable = """
                CREATE TABLE IF NOT EXISTS ReadingHabit (
                    id INTEGER PRIMARY KEY,
                    user_id INTEGER,
                    book_id INTEGER,
                    pages_read INTEGER,
                    submission_moment TEXT,
                    FOREIGN KEY (user_id) REFERENCES User(id),
                    FOREIGN KEY (book_id) REFERENCES Book(id)
                )
                """;

        Connection connection = connect();
        if (connection == null) {
            return;
        }

        try (connection;
             PreparedStatement userStatement = connection.prepareStatement(createUserTable);
             PreparedStatement bookStatement = connection.prepareStatement(createBookTable);
             PreparedStatement readingHabitStatement = connection.prepareStatement(createReadingHabitTable)) {
            userStatement.execute();
            bookStatement.execute();
            readingHabitStatement.execute();
            ensureColumnExists(connection, "User", "gender", "TEXT");
            ensureColumnExists(connection, "User", "Name", "TEXT");
            ensureColumnExists(connection, "ReadingHabit", "submission_moment", "TEXT");
            System.out.println("Tables are ready.");
        } catch (SQLException e) {
            System.out.println("Could not create tables: " + e.getMessage());
        }
    }

    public void importDatasetIfNeeded() {
        File datasetFile = new File(DATASET_PATH);
        if (!datasetFile.exists()) {
            System.out.println("Dataset file not found: " + DATASET_PATH);
            return;
        }

        if (isDatasetAlreadyImported()) {
            System.out.println("Dataset already imported.");
            return;
        }

        clearExistingData();
        if (importDataset(datasetFile)) {
            System.out.println("Dataset imported successfully.");
        } else {
            System.out.println("Dataset import did not complete.");
        }
    }

    public void addUser(int age, String gender, String name) {
        String sql = "INSERT INTO User(age, gender, Name) VALUES(?, ?, ?)";

        try (Connection connection = DriverManager.getConnection(URL);
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, age);
            statement.setString(2, gender);
            statement.setString(3, name);
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int newUserId = generatedKeys.getInt(1);
                    System.out.println("User added successfully with ID: " + newUserId);
                } else {
                    System.out.println("User added successfully.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error inserting user: " + e.getMessage());
        }
    }

    public void addBook(String title) {
        String sql = "INSERT INTO Book(title) VALUES(?)";

        try (Connection connection = DriverManager.getConnection(URL);
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, title);
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int newBookId = generatedKeys.getInt(1);
                    System.out.println("Book added successfully with ID: " + newBookId);
                } else {
                    System.out.println("Book added successfully.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error inserting book: " + e.getMessage());
        }
    }

    public void addReadingHabit(int userId, int bookId, int pagesRead, String submissionMoment) {
        String findNextIdSql = "SELECT COALESCE(MAX(id), 0) + 1 AS next_id FROM ReadingHabit";
        String insertHabitSql = """
                INSERT INTO ReadingHabit(id, user_id, book_id, pages_read, submission_moment)
                VALUES(?, ?, ?, ?, ?)
                """;

        try (Connection connection = DriverManager.getConnection(URL);
             PreparedStatement nextIdStatement = connection.prepareStatement(findNextIdSql);
             ResultSet nextIdResult = nextIdStatement.executeQuery()) {

            int nextHabitId = nextIdResult.getInt("next_id");

            try (PreparedStatement insertStatement = connection.prepareStatement(insertHabitSql)) {
                insertStatement.setInt(1, nextHabitId);
                insertStatement.setInt(2, userId);
                insertStatement.setInt(3, bookId);
                insertStatement.setInt(4, pagesRead);
                insertStatement.setString(5, submissionMoment);
                insertStatement.executeUpdate();
                System.out.println("Reading habit added successfully with ID: " + nextHabitId);
            }
        } catch (SQLException e) {
            System.out.println("Error inserting reading habit: " + e.getMessage());
        }
    }

    public void viewReadingHabitsForUser(int userId) {
        String sql = """
                SELECT ReadingHabit.id, ReadingHabit.user_id, Book.title, ReadingHabit.pages_read, ReadingHabit.submission_moment
                FROM ReadingHabit
                JOIN Book ON ReadingHabit.book_id = Book.id
                WHERE user_id = ?
                """;

        try (Connection connection = DriverManager.getConnection(URL);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                boolean found = false;

                while (resultSet.next()) {
                    found = true;
                    System.out.println(
                            "Habit ID: " + resultSet.getInt("id")
                                    + ", User ID: " + resultSet.getInt("user_id")
                                    + ", Book: " + resultSet.getString("title")
                                    + ", Pages read: " + resultSet.getInt("pages_read")
                                    + ", Submitted: " + resultSet.getString("submission_moment")
                    );
                }

                if (!found) {
                    System.out.println("No reading habits found for this user.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving reading habits: " + e.getMessage());
        }
    }

    public void updateBookTitle(int bookId, String newTitle) {
        String sql = "UPDATE Book SET title = ? WHERE id = ?";

        try (Connection connection = DriverManager.getConnection(URL);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, newTitle);
            statement.setInt(2, bookId);
            int rowsUpdated = statement.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("Book title updated successfully.");
            } else {
                System.out.println("No book found with that id.");
            }
        } catch (SQLException e) {
            System.out.println("Error updating book title: " + e.getMessage());
        }
    }

    public void deleteReadingHabit(int readingHabitId) {
        String sql = "DELETE FROM ReadingHabit WHERE id = ?";

        try (Connection connection = DriverManager.getConnection(URL);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, readingHabitId);
            int rowsDeleted = statement.executeUpdate();

            if (rowsDeleted > 0) {
                System.out.println("Reading habit deleted successfully.");
            } else {
                System.out.println("No reading habit found with that id.");
            }
        } catch (SQLException e) {
            System.out.println("Error deleting reading habit: " + e.getMessage());
        }
    }

    public void showMeanAgeOfUsers() {
        String sql = "SELECT AVG(age) AS mean_age FROM User";

        try (Connection connection = DriverManager.getConnection(URL);
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            double meanAge = resultSet.getDouble("mean_age");

            if (resultSet.wasNull()) {
                System.out.println("No users found.");
            } else {
                System.out.println("Mean age of users: " + meanAge);
            }
        } catch (SQLException e) {
            System.out.println("Could not calculate mean age: " + e.getMessage());
        }
    }

    public void countUsersWhoReadSpecificBook(int bookId) {
        String sql = """
                SELECT COUNT(DISTINCT user_id) AS user_count
                FROM ReadingHabit
                WHERE book_id = ?
                """;

        try (Connection connection = DriverManager.getConnection(URL);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, bookId);

            try (ResultSet resultSet = statement.executeQuery()) {
                int count = resultSet.getInt("user_count");
                System.out.println("Number of users who read book " + bookId + ": " + count);
            }
        } catch (SQLException e) {
            System.out.println("Error counting users: " + e.getMessage());
        }
    }

    public int showTotalPagesRead() {
        String sql = "SELECT SUM(pages_read) AS total_pages FROM ReadingHabit";

        try (Connection connection = DriverManager.getConnection(URL);
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            int totalPages = resultSet.getInt("total_pages");

            if (resultSet.wasNull()) {
                System.out.println("No reading habits found.");
                return 0;
            } else {
                System.out.println("Total pages read: " + totalPages);
                return totalPages;
            }
        } catch (SQLException e) {
            System.out.println("Could not calculate total pages: " + e.getMessage());
            return 0;
        }
    }

    public void showUsersWhoReadMoreThanOneBook() {
        String sql = """
                SELECT COUNT(*) AS user_count
                FROM (
                    SELECT user_id
                    FROM ReadingHabit
                    GROUP BY user_id
                    HAVING COUNT(DISTINCT book_id) > 1
                ) AS grouped_users
                """;

        try (Connection connection = DriverManager.getConnection(URL);
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            int userCount = resultSet.getInt("user_count");
            System.out.println("Users who have read more than one book: " + userCount);
        } catch (SQLException e) {
            System.out.println("Error counting users: " + e.getMessage());
        }
    }

    private void ensureColumnExists(Connection connection, String tableName, String columnName, String columnType)
            throws SQLException {
        String pragmaSql = "PRAGMA table_info(" + tableName + ")";

        try (PreparedStatement statement = connection.prepareStatement(pragmaSql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                if (columnName.equalsIgnoreCase(resultSet.getString("name"))) {
                    return;
                }
            }
        }

        String alterSql = "ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + columnType;
        try (PreparedStatement statement = connection.prepareStatement(alterSql)) {
            statement.execute();
        }
    }

    private boolean isDatasetAlreadyImported() {
        String userCountSql = "SELECT COUNT(*) AS total FROM User";
        String habitCountSql = "SELECT COUNT(*) AS total FROM ReadingHabit";
        String bookCountSql = "SELECT COUNT(*) AS total FROM Book";

        try (Connection connection = DriverManager.getConnection(URL);
             PreparedStatement userStatement = connection.prepareStatement(userCountSql);
             PreparedStatement habitStatement = connection.prepareStatement(habitCountSql);
             PreparedStatement bookStatement = connection.prepareStatement(bookCountSql);
             ResultSet userResult = userStatement.executeQuery();
             ResultSet habitResult = habitStatement.executeQuery();
             ResultSet bookResult = bookStatement.executeQuery()) {

            int userCount = userResult.getInt("total");
            int habitCount = habitResult.getInt("total");
            int bookCount = bookResult.getInt("total");

            return userCount >= 65 && habitCount >= 100 && bookCount > 0;
        } catch (SQLException e) {
            System.out.println("Could not check dataset import status: " + e.getMessage());
            return false;
        }
    }

    private void clearExistingData() {
        String deleteReadingHabits = "DELETE FROM ReadingHabit";
        String deleteBooks = "DELETE FROM Book";
        String deleteUsers = "DELETE FROM User";
        String resetBookSequence = "DELETE FROM sqlite_sequence WHERE name = 'Book'";

        try (Connection connection = DriverManager.getConnection(URL);
             PreparedStatement readingHabitStatement = connection.prepareStatement(deleteReadingHabits);
             PreparedStatement bookStatement = connection.prepareStatement(deleteBooks);
             PreparedStatement userStatement = connection.prepareStatement(deleteUsers)) {

            readingHabitStatement.executeUpdate();
            bookStatement.executeUpdate();
            userStatement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Could not clear existing data: " + e.getMessage());
        }

        try (Connection connection = DriverManager.getConnection(URL);
             PreparedStatement resetSequenceStatement = connection.prepareStatement(resetBookSequence)) {
            resetSequenceStatement.executeUpdate();
        } catch (SQLException e) {
            // sqlite_sequence may not exist yet, which is fine for a fresh database.
        }
    }

    private boolean importDataset(File datasetFile) {
        String sql = "INSERT INTO User(id, age, gender) VALUES(?, ?, ?)";
        String insertBookSql = "INSERT OR IGNORE INTO Book(title) VALUES(?)";
        String findBookSql = "SELECT id FROM Book WHERE title = ?";
        String insertHabitSql = """
                INSERT INTO ReadingHabit(id, user_id, book_id, pages_read, submission_moment)
                VALUES(?, ?, ?, ?, ?)
                """;

        try (Workbook workbook = WorkbookFactory.create(datasetFile);
             Connection connection = DriverManager.getConnection(URL);
             PreparedStatement insertUserStatement = connection.prepareStatement(sql);
             PreparedStatement insertBookStatement = connection.prepareStatement(insertBookSql);
             PreparedStatement findBookStatement = connection.prepareStatement(findBookSql);
             PreparedStatement insertHabitStatement = connection.prepareStatement(insertHabitSql)) {

            DataFormatter formatter = new DataFormatter();
            importUsersFromSheet(workbook.getSheet("User"), formatter, insertUserStatement);
            importReadingHabitsFromSheet(workbook.getSheet("ReadingHabit"), formatter,
                    insertBookStatement, findBookStatement, insertHabitStatement);
            return true;
        } catch (IOException | SQLException e) {
            System.out.println("Could not import dataset: " + e.getMessage());
            return false;
        }
    }

    private void importUsersFromSheet(Sheet sheet, DataFormatter formatter, PreparedStatement statement)
            throws SQLException {
        for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row == null) {
                continue;
            }

            int id = Integer.parseInt(formatter.formatCellValue(row.getCell(0)));
            int age = Integer.parseInt(formatter.formatCellValue(row.getCell(1)));
            String gender = formatter.formatCellValue(row.getCell(2));

            statement.setInt(1, id);
            statement.setInt(2, age);
            statement.setString(3, gender);
            statement.executeUpdate();
        }
    }

    private void importReadingHabitsFromSheet(
            Sheet sheet,
            DataFormatter formatter,
            PreparedStatement insertBookStatement,
            PreparedStatement findBookStatement,
            PreparedStatement insertHabitStatement
    ) throws SQLException {
        for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row == null) {
                continue;
            }

            int habitId = Integer.parseInt(formatter.formatCellValue(row.getCell(0)));
            int userId = Integer.parseInt(formatter.formatCellValue(row.getCell(1)));
            int pagesRead = Integer.parseInt(formatter.formatCellValue(row.getCell(2)));
            String bookTitle = formatter.formatCellValue(row.getCell(3));
            String submissionMoment = formatter.formatCellValue(row.getCell(4));

            insertBookStatement.setString(1, bookTitle);
            insertBookStatement.executeUpdate();

            findBookStatement.setString(1, bookTitle);
            try (ResultSet resultSet = findBookStatement.executeQuery()) {
                if (resultSet.next()) {
                    int bookId = resultSet.getInt("id");

                    insertHabitStatement.setInt(1, habitId);
                    insertHabitStatement.setInt(2, userId);
                    insertHabitStatement.setInt(3, bookId);
                    insertHabitStatement.setInt(4, pagesRead);
                    insertHabitStatement.setString(5, submissionMoment);
                    insertHabitStatement.executeUpdate();
                }
            }
        }
    }
}
