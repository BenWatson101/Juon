package JUOM.Web;

import java.util.Dictionary;
import java.util.Hashtable;

public record Resource(String content, String mime) {

    public static Dictionary<String, String> extensionToMIME = new Hashtable<>();

    static {
        extensionToMIME.put("html", "text/html");
        extensionToMIME.put("css", "text/css");
        extensionToMIME.put("js", "application/javascript");
        extensionToMIME.put("png", "image/png");
        extensionToMIME.put("jpg", "image/jpeg");
        extensionToMIME.put("gif", "image/gif");
        extensionToMIME.put("ico", "image/x-icon");
    }

    public Resource {
        if (content == null) {
            throw new IllegalArgumentException("Resource cannot be null");
        }
        if (mime == null) {
            throw new IllegalArgumentException("Mime cannot be null");
        }
    }
}
