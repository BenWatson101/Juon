package JUOM.Web;

import JUOM.JHTML.JHTML;
import JUOM.UniversalObjects.Universal;
import JUOM.UniversalObjects.UniversalObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Objects;

public abstract class ServerObject extends UniversalObject {

    protected record Resource(String resource, String mime) {}

    protected static Dictionary<String, String> extensionToMIME = new Hashtable<>();
    static {
        extensionToMIME.put("html", "text/html");
        extensionToMIME.put("css", "text/css");
        extensionToMIME.put("js", "application/javascript");
        extensionToMIME.put("png", "image/png");
        extensionToMIME.put("jpg", "image/jpeg");
        extensionToMIME.put("gif", "image/gif");
    }

    protected final void parseParams(Server.Client c, String paramString) throws IOException {
        try {
            paramString = URLDecoder.decode(paramString.substring(1), StandardCharsets.UTF_8);

            String[] paramsAndName = paramString.split("\\Q<?>\\E", 2);
            String methodName = paramsAndName[0];

            try {
                String[] params = paramsAndName[1].split("\\Q<?>\\E");

                if(params.length == 1 && params[0].isEmpty()) {
                    params = new String[0];
                }

                Object[] objects = UniversalObject.parse(params);

                if(methodName.equals(this.getClass().getName())) {
                    Constructor<?>[] constructors = this.getClass().getConstructors();
                    for (Constructor<?> constructor : constructors) {
                        if(constructor.getParameterCount() == objects.length
                                && constructor.isAnnotationPresent(Universal.class)) {

                            sendUniversalResponse(c, (UniversalObject) constructor.newInstance(objects));
                            return;
                        }
                    }
                } else {
                    for (Method method : this.getClass().getDeclaredMethods()) {
                        if (method.getName().equals(methodName)
                                && method.getParameterCount() == objects.length
                                && method.isAnnotationPresent(Universal.class)) {

                            method.setAccessible(true);

                            sendUniversalResponse(c, UniversalObject.convert(method.invoke(this, objects)));
                            return;
                        }
                    }
                }
            } catch (Exception e) {
                sendNotFoundResponse(c, "Method called " + paramsAndName[0] +  " for page "
                        + this.getClass().getName() + " not found");
                e.printStackTrace();
                return;
            }
            throw new Exception();
        } catch (Exception e) {
            sendNotFoundResponse(c, "Invalid parameters when executing page methods for page "
                    + this.getClass().getName() + " error::\n" + Arrays.toString(e.getStackTrace()));
        }
    }


    Resource handleResource(String path) throws IOException {

        if(path.charAt(0) == '.') {
            throw new IOException("Invalid path");
        }

        if(path.contains(".")) {
            String[] split = path.split("\\.");
            String extension = split[split.length - 1];

            String mime = extensionToMIME.get(extension);

            if(mime == null) {
                throw new IOException("Unknown file extension");
            }

            if (path.contains("..") || new File(path.substring(1)).isAbsolute()) {
                throw new IOException("Invalid path");
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream(path))));

            StringBuilder content = new StringBuilder();
            String line;
            while((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            reader.close();
            return new Resource(content.toString(), mime);

        } else {
            throw new IOException("No file extension");
        }
    }

    protected final void sendResponse(Server.Client c, Resource r, int code) throws IOException {
        c.out.write("HTTP/1.1 " + code + " OK\r\n" +
                "Content-Type: " + r.mime() + "\r\n" +
                "\r\n" +
                r.resource());
        c.out.flush();
    }

    protected final void sendNotFoundResponse(Server.Client c, String message) throws IOException {
        sendHTMLResponse(c, pageNotFound(message));
    }

    protected final void sendResourceResponse(Server.Client c, Resource resource) throws IOException {
        sendResponse(c, resource, 200);
    }

    protected final void sendHTMLResponse(Server.Client c, JHTML html) throws IOException {
        sendResponse(c, new Resource(html.html(), "text/html"), 200);
    }

    protected final void sendUniversalResponse(Server.Client c, UniversalObject obj) throws IOException {
        sendResponse(c, new Resource(obj.json(), "application/json"), 200);
    }

    protected final void sendNoResponse(Server.Client c) throws IOException {
        sendResponse(c, new Resource("", "text/html"), 200);
    }

    protected abstract JHTML pageNotFound(String message);

    protected abstract void handleURL(Server.Client c, String url) throws IOException;
}
