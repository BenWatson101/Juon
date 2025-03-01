package JUOM.Web;

import java.io.*;

import JUOM.JHTML.JHTML;
import JUOM.UniversalObjects.UniversalObject;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Objects;

public abstract class Page extends UniversalObject {

    record Resource(String resource, String mime) {}

    protected static Dictionary<String, String> extensionToMIME = new Hashtable<>();
    static {
        extensionToMIME.put("html", "text/html");
        extensionToMIME.put("css", "text/css");
        extensionToMIME.put("js", "application/javascript");
        extensionToMIME.put("png", "image/png");
        extensionToMIME.put("jpg", "image/jpeg");
        extensionToMIME.put("gif", "image/gif");
    }

    protected abstract JHTML startingPage();
    protected abstract JHTML pageNotFound(String message);

    Resource handleResource(String path) throws Exception {

        if(path.charAt(0) == '.') {
            throw new Exception("Invalid path");
        }

        if(path.contains(".")) {
            String[] split = path.split("\\.");
            String extension = split[split.length - 1];

            String mime = extensionToMIME.get(extension);

            if(mime == null) {
                throw new Exception("Unknown file extension");
            }

            if (path.contains("..") || new File(path).isAbsolute()) {
                throw new Exception("Invalid path");
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream("/" + path))));

            StringBuilder content = new StringBuilder();
            String line;
            while((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            reader.close();
            return new Resource(content.toString(), mime);

        } else {
            throw new Exception("No file extension");
        }
    }
}
