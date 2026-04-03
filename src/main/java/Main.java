import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Database database = new Database();
        database.createTables();
        database.importDatasetIfNeeded();

        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            printMenu();
            System.out.print("Choose an option: ");
            String input = scanner.nextLine();

            try {
                switch (input) {
                    case "1":
                        addUser(scanner, database);
                        break;
                    case "2":
                        viewReadingHabits(scanner, database);
                        break;
                    case "3":
                        updateBookTitle(scanner, database);
                        break;
                    case "4":
                        deleteReadingHabit(scanner, database);
                        break;
                    case "5":
                        database.showMeanAgeOfUsers();
                        break;
                    case "6":
                        countUsersWhoReadBook(scanner, database);
                        break;
                    case "7":
                        database.showTotalPagesRead();
                        break;
                    case "8":
                        database.showUsersWhoReadMoreThanOneBook();
                        break;
                    case "9":
                        addBook(scanner, database);
                        break;
                    case "10":
                        addReadingHabit(scanner, database);
                        break;
                    case "11":
                        running = false;
                        System.out.println("Goodbye!");
                        break;
                    default:
                        System.out.println("Invalid option. Please try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }

            System.out.println();
        }

        scanner.close();
    }

    private static void printMenu() {
        System.out.println("=== Book Tracker Menu ===");
        System.out.println("1. Add user");
        System.out.println("2. View reading habits for a user");
        System.out.println("3. Update book title");
        System.out.println("4. Delete reading habit");
        System.out.println("5. Mean age of users");
        System.out.println("6. Count users who read a specific book");
        System.out.println("7. Total pages read");
        System.out.println("8. Users who read more than one book");
        System.out.println("9. Add book");
        System.out.println("10. Add reading habit");
        System.out.println("11. Exit");
    }

    private static void addUser(Scanner scanner, Database database) {
        System.out.print("Enter user age: ");
        int age = Integer.parseInt(scanner.nextLine());

        System.out.print("Enter user gender: ");
        String gender = scanner.nextLine();

        System.out.print("Enter user name: ");
        String name = scanner.nextLine();

        database.addUser(age, gender, name);
    }

    private static void viewReadingHabits(Scanner scanner, Database database) {
        System.out.print("Enter user id: ");
        int userId = Integer.parseInt(scanner.nextLine());
        database.viewReadingHabitsForUser(userId);
    }

    private static void updateBookTitle(Scanner scanner, Database database) {
        System.out.print("Enter book id: ");
        int bookId = Integer.parseInt(scanner.nextLine());

        System.out.print("Enter new title: ");
        String newTitle = scanner.nextLine();

        database.updateBookTitle(bookId, newTitle);
    }

    private static void deleteReadingHabit(Scanner scanner, Database database) {
        System.out.print("Enter reading habit id: ");
        int readingHabitId = Integer.parseInt(scanner.nextLine());
        database.deleteReadingHabit(readingHabitId);
    }

    private static void countUsersWhoReadBook(Scanner scanner, Database database) {
        System.out.print("Enter book id: ");
        int bookId = Integer.parseInt(scanner.nextLine());
        database.countUsersWhoReadSpecificBook(bookId);
    }

    private static void addBook(Scanner scanner, Database database) {
        System.out.print("Enter book title: ");
        String title = scanner.nextLine();
        database.addBook(title);
    }

    private static void addReadingHabit(Scanner scanner, Database database) {
        System.out.print("Enter user id: ");
        int userId = Integer.parseInt(scanner.nextLine());

        System.out.print("Enter book id: ");
        int bookId = Integer.parseInt(scanner.nextLine());

        System.out.print("Enter pages read: ");
        int pagesRead = Integer.parseInt(scanner.nextLine());

        System.out.print("Enter submission moment: ");
        String submissionMoment = scanner.nextLine();

        database.addReadingHabit(userId, bookId, pagesRead, submissionMoment);
    }
}
