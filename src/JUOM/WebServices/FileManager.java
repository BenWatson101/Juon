package JUOM.WebServices;

import java.io.IOException;
import java.io.InputStream;

public class FileManager {

    public static byte[] readFile(String path, Class<?> clazz) throws IOException {
        try (InputStream e = clazz.getResourceAsStream(path)) {
            if (e == null) {
                return null;
            }
            return e.readAllBytes();
        }
    }

    public static void writeFile(String path, byte[] bytes, Class<?> clazz) throws IOException {
        java.nio.file.Path resourcePath = java.nio.file.Paths.get(clazz.getResource("/").getPath(), path);
        java.nio.file.Files.createDirectories(resourcePath.getParent());
        java.nio.file.Files.write(resourcePath, bytes);
    }

}
