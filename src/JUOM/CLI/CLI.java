package JUOM.CLI;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class CLI {
    private static final Map<String, Command> commands = new HashMap<>();

    static {
        commands.put("create-page", args -> {
            if (args.length < 2) {
                System.out.println("Usage: create-page <className> <path>");
                return;
            }
            // TODO: Generate page class template
            System.out.println("Creating new page: " + args[0]);
        });

        commands.put("list-pages", args -> {
            // TODO: Scan project directory for Page classes
            System.out.println("Listing all pages...");
        });

        commands.put("help", args -> {
            System.out.println("Available commands:");
            System.out.println("  create-page <className> <path> - Creates a new page class");
            System.out.println("  list-pages                    - Lists all pages in the project");
            System.out.println("  help                         - Shows this help message");
            System.out.println("  exit                         - Exits the CLI");
        });
    }

    @FunctionalInterface
    private interface Command {
        void execute(String[] args);
    }

    public static void main(String[] args) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            System.out.print("JUOM> ");
            try {
                String input = reader.readLine();
                if (input == null || input.equalsIgnoreCase("exit")) {
                    break;
                }

                String[] parts = input.trim().split("\\s+");
                String commandName = parts[0].toLowerCase();
                String[] commandArgs = new String[parts.length - 1];
                System.arraycopy(parts, 1, commandArgs, 0, parts.length - 1);

                Command command = commands.get(commandName);
                if (command != null) {
                    command.execute(commandArgs);
                } else {
                    System.out.println("Unknown command. Type 'help' for available commands.");
                }
            } catch (IOException e) {
                System.out.println("Error reading command: " + e.getMessage());
            }
        }
    }
}