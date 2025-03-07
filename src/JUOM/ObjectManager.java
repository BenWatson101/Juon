package JUOM;

import JUOM.Utils.JarChecker;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class ObjectManager {
    private static final Map<String, Command> commands = new HashMap<>();

    static {
        commands.put("extract-required", args -> {
            if(args.length < 1) {
                System.out.println("Usage: extract-required <path-to-src>");
            } else {
                if (JarChecker.isRunningFromJar()) {

                    String src = new File(System.getProperty("user.dir"), args[0]).getAbsolutePath();
                    if (!new File(src).exists()) {
                        System.out.println("Source directory not found: " + src);
                        return;
                    }

                    File dir = new File(src,"Required");
                    dir.mkdirs();
                    extractRequired("/JUOM/Required/UniversalObject.js", "UniversalObject.js", dir);
                    extractRequired("/JUOM/Required/Required.txt", "Required.java", dir);
                } else {
                    System.out.println("ERROR Not running from JAR");
                }
            }
        });

        commands.put("create", args -> {
            if (args.length < 3) {
                System.out.println("Usage: create-page <type> <name>");

                System.out.println("\nAvailable types:");
                System.out.println("UniversalObject");
                System.out.println("Page");

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
            System.out.println("  extract-required     - Extracts necessary files for development");
            System.out.println("  list-objects         - Lists all objects in the project");
            System.out.println("  help                 - Shows this help message");
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

    private static void extractRequired(String sourceFileName, String destinationFileName, File folder) {
        try (PrintWriter writer = new PrintWriter(new File(folder,destinationFileName))) {
            InputStream e = ObjectManager.class.getResourceAsStream(sourceFileName);

            if (e == null) {
                throw new FileNotFoundException("File not found");
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(e));

            String line;
            while ((line = reader.readLine()) != null) {
                writer.println(line);
            }

            reader.close();

        } catch (IOException e) {
            System.out.println("An error occurred while writing to the file: " + e.getMessage());
        }
    }
}