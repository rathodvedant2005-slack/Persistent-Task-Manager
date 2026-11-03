import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

// Stores the information related to each task.
class Task {

    private static int nextId = 1;

    private final int id;
    private String title;
    private String description;
    private String status;

    // New tasks are given an ID and start with Pending status.
    public Task(String title, String description) {
        this(nextId, title, description, "Pending");
    }

    public Task(int id, String title, String description, String status) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;

        if (id >= nextId) {
            nextId = id + 1;
        }
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Converts task details into a format that can be saved in a file.
    public String toFileLine() {
        return id + "|" + title + "|" + description + "|" + status;
    }

    @Override
    public String toString() {
        return "ID: " + id
                + " | Title: " + title
                + " | Description: " + description
                + " | Status: " + status;
    }
}

public class PersistentTaskManager {

    private static final String FILE_NAME = "tasks.txt";

    private static final List<Task> tasks = new ArrayList<>();
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {

        loadTasks();

        int choice;

        do {
            showMenu();
            choice = readInt("Choose an option: ");

            switch (choice) {
                case 1 -> createTask();
                case 2 -> readTasks();
                case 3 -> updateTask();
                case 4 -> deleteTask();
                case 5 -> System.out.println("Thanks for using Task Manager!");

                default -> System.out.println(
                        "Invalid option. Please choose a number from 1 to 5.");
            }

        } while (choice != 5);

        scanner.close();
    }

    // Displays the available task management options.
    private static void showMenu() {
        System.out.println("\n===== TASK MANAGER =====");
        System.out.println("1. Create a Task");
        System.out.println("2. View All Tasks");
        System.out.println("3. Update a Task");
        System.out.println("4. Delete a Task");
        System.out.println("5. Exit");
    }

    // Reads saved tasks from the file when the application starts.
    private static void loadTasks() {

        try (BufferedReader reader =
                     new BufferedReader(new FileReader(FILE_NAME))) {

            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;

                if (line.trim().isEmpty()) {
                    continue;
                }

                try {
                    String[] details = line.split("\\|", -1);

                    if (details.length != 4) {
                        throw new IllegalArgumentException();
                    }

                    int id = Integer.parseInt(details[0].trim());

                    Task task = new Task(
                            id,
                            details[1],
                            details[2],
                            details[3]
                    );

                    tasks.add(task);

                } catch (IllegalArgumentException e) {
                    System.out.println(
                            "Skipping an invalid entry on line " + lineNumber + ".");
                }
            }

            System.out.println("Tasks loaded: " + tasks.size());

        } catch (FileNotFoundException e) {
            System.out.println(
                    "No saved tasks found. You can create your first task!");

        } catch (IOException e) {
            System.out.println(
                    "Unable to load tasks: " + e.getMessage());
        }
    }

    private static void saveTasks() {

        try (BufferedWriter writer =
                     new BufferedWriter(new FileWriter(FILE_NAME))) {

            for (Task task : tasks) {
                writer.write(task.toFileLine());
                writer.newLine();
            }

        } catch (IOException e) {
            System.out.println(
                    "Could not save your tasks: " + e.getMessage());
        }
    }

    // Creates a new task using the details entered by the user.
    private static void createTask() {

        String title = clean(readNonEmpty("Enter task title: "));

        System.out.print("Enter task description: ");
        String description = clean(scanner.nextLine().trim());

        Task newTask = new Task(title, description);
        tasks.add(newTask);

        saveTasks();

        System.out.println(
                "Your task has been added! ID: " + newTask.getId());
    }

    // Displays all the tasks currently available.
    private static void readTasks() {

        if (tasks.isEmpty()) {
            System.out.println("Your task list is empty.");
            return;
        }

        System.out.println("\n--- YOUR TASKS ---");

        for (Task task : tasks) {
            System.out.println(task);
        }
    }

    // Finds a task and allows the user to change its details.
    private static void updateTask() {

        if (tasks.isEmpty()) {
            System.out.println("There are no tasks to update.");
            return;
        }

        int id = readInt("Enter the ID of the task you want to update: ");
        Task task = findById(id);

        if (task == null) {
            System.out.println("Sorry, that task was not found.");
            return;
        }

        System.out.println("\nCurrent task: " + task);
        System.out.println("Press Enter to keep the existing information.");

        System.out.print("Enter new title: ");
        String title = clean(scanner.nextLine().trim());

        if (!title.isEmpty()) {
            task.setTitle(title);
        }

        System.out.print("Enter new description: ");
        String description = clean(scanner.nextLine().trim());

        if (!description.isEmpty()) {
            task.setDescription(description);
        }

        System.out.println("\nChoose a status:");
        System.out.println("1. Pending");
        System.out.println("2. In Progress");
        System.out.println("3. Completed");
        System.out.println("Press Enter to keep the current status.");

        System.out.print("Enter your choice: ");
        String choice = scanner.nextLine().trim();

        switch (choice) {
            case "1" -> task.setStatus("Pending");
            case "2" -> task.setStatus("In Progress");
            case "3" -> task.setStatus("Completed");

            case "" -> {
                // Keep the current status if no option is selected.
            }

            default -> System.out.println(
                    "Invalid status. Keeping the previous one.");
        }

        saveTasks();
        System.out.println("Task updated successfully!");
    }

    // Removes a task using its ID.
    private static void deleteTask() {

        if (tasks.isEmpty()) {
            System.out.println("There are no tasks to delete.");
            return;
        }

        int id = readInt("Enter the ID of the task you want to delete: ");
        Task task = findById(id);

        if (task == null) {
            System.out.println("Sorry, that task was not found.");
            return;
        }

        tasks.remove(task);
        saveTasks();

        System.out.println("Task deleted successfully!");
    }

    // Searches the task list and returns the matching task.
    private static Task findById(int id) {

        for (Task task : tasks) {
            if (task.getId() == id) {
                return task;
            }
        }

        return null;
    }

    private static String clean(String text) {
        return text.replace('|', '/');
    }

    // Keeps asking until the user enters a valid number.
    private static int readInt(String prompt) {

        while (true) {
            System.out.print(prompt);

            try {
                return Integer.parseInt(scanner.nextLine().trim());

            } catch (NumberFormatException e) {
                System.out.println(
                        "Please enter a valid number and try again.");
            }
        }
    }

    // Makes sure the task title is not left empty.
    private static String readNonEmpty(String prompt) {

        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            if (!input.isEmpty()) {
                return input;
            }

            System.out.println(
                    "Task title cannot be empty. Please try again.");
        }
    }
}