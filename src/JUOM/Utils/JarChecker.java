package JUOM.Utils;

import java.net.URL;

public class JarChecker {
    public static boolean isRunningFromJar() {
        URL resource = JarChecker.class.getResource("/" + JarChecker.class.getName().replace('.', '/') + ".class");
        return resource != null && resource.getProtocol().equals("jar");
    }
}
