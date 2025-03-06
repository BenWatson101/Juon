package JUOM;

import java.util.HashMap;
import java.util.Map;

public class ObjectManager {
    private static final Map<String, Command> commands = new HashMap<>();

    static {
        commands.put("create", args -> {
            if (args.length < 2) {
                System.out.println("Usage: create-page <type> <name>");

                System.out.println("\nAvailable types:");
                System.out.println("UniversalObject");
                System.out.println("Page");

                //String jarDir = new File(ObjectManager.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent();
                //System.out.println("JAR Directory: " + jarDir);

                return;
            }
            // TODO: Generate page class template
        });

        commands.put("list-objects", args -> {
            // TODO: Scan project directory for Page classes
            System.out.println("Listing all objects...");
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
        if (args.length == 0) {
            System.out.println("No command provided. Type 'help' for available commands.");
            return;
        }

        String commandName = args[0].toLowerCase();
        String[] commandArgs = new String[args.length - 1];
        System.arraycopy(args, 1, commandArgs, 0, args.length - 1);

        Command command = commands.get(commandName);
        if (command != null) {
            command.execute(commandArgs);
        } else {
            System.out.println("Unknown command. Type 'help' for available commands.");
        }
    }
}