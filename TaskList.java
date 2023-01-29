package com.codurance.training.tasks;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream;

public final class TaskList implements Runnable {
    private static final String QUIT = "quit";

    private final Map<String, List<Task>> taskMap = new LinkedHashMap<>();
    private final BufferedReader inputReader;
    private final PrintWriter outputWriter;

    private long lastTaskId = 0;

    public static void main(String[] args) throws Exception {
        BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter outputWriter = new PrintWriter(System.out);
        new TaskList(inputReader, outputWriter).run();
    }

    public TaskList(BufferedReader inputReader, PrintWriter outputWriter) {
        this.inputReader = inputReader;
        this.outputWriter = outputWriter;
    }

    public void run() {
        while (true) {
            outputWriter.print("> ");
            outputWriter.flush();
            String command;
            try {
                command = inputReader.readLine();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (command.equals(QUIT)) {
                break;
            }
            executeCommand(command);
        }
    }

    private void execute_Command(String commandLine) {
            String[] commandAndRest = commandLine.split(" ", 2);
            String command = commandAndRest[0];
            Stream.of(new AbstractMap.SimpleEntry<>("show", this::showTask),
                    new AbstractMap.SimpleEntry<>("add", () -> addCommand(commandAndRest[1])),
                    new AbstractMap.SimpleEntry<>("check", () -> checkTask(commandAndRest[1])),
                    new AbstractMap.SimpleEntry<>("uncheck", () -> uncheckTask(commandAndRest[1])),
                    new AbstractMap.SimpleEntry<>("help", this::displayHelp),
                    new AbstractMap.SimpleEntry<>("quit", () -> {/*do nothing*/}))
                    .filter(entry -> entry.getKey().equals(command))
                    .map(Map.Entry::getValue)
                    .findFirst()
                    .orElse(() -> displayError(command))
                    .run();
    }

    private void show_Tasks() {
            taskMap.forEach((projectName, tasks) -> {
                outputWriter.println(projectName);
                tasks.forEach(task -> outputWriter.printf("    [%c] %d: %s%n", (task.done ? 'x' : ' '), task.id, task.description));
                outputWriter.println();
            });
    }


    private void add_Command(String commandLine) {
        String[] subcommandAndRest = commandLine.split(" ", 2);
        Stream.of("project", "tasks")
                .filter(subcommand -> subcommand.equals(subcommandAndRest[0]))
                .findFirst()
                .ifPresent(subcommand -> {
                    if (subcommand.equals("project")) {
                        addProject(subcommandAndRest[1]);
                    } else {
                        addTask(subcommandAndRest[1]);
                    }
                });
    }


    private void add_Project(String name) {
        taskMap.put(name, new ArrayList<Task>());
    }

    private void add_Tasks(String project, String description) {
        taskMap.entrySet().stream()
                .filter(entry -> entry.getKey().equals(project))
                .findFirst()
                .ifPresentOrElse(entry -> entry.getValue().add(new Task(nextTaskId(), description, false)),
                        () -> outputWriter.printf("Could not find a project with the name \"%s\".", project));
    }


    private void check_Tasks(String idString) {
        setTaskDone(idString, true);
    }

    private void uncheck_Tasks(String idString) {
        setTaskDone(idString, false);
    }
    private void set_Tasks(String idString, boolean done) {
        int id = Integer.parseInt(idString);
        taskMap.values().stream()
                .flatMap(List::stream)
                .filter(task -> task.id == id)
                .findFirst()
                .ifPresent(task -> task.done=done);
    }


    private void Help() {
        outputWriter.println("Command:");
        VALID_COMMANDS.forEach(command -> outputWriter.println("  " + command));
    }

    private static final List<String> VALID_COMMANDS = Arrays.asList("show_Tasks", "add_Project", "add_Tasks", "check_Tasks", "uncheck_Tasks", "help", "quit");
    private void Error(String command) {
        if (!VALID_COMMANDS.stream().anyMatch(validCommand -> validCommand.equals(command))) {
            outputWriter.printf("ERROR: %s", command);
            outputWriter.println();
        }
    }


}
    
