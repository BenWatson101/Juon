package JUOM.Web;

import JUOM.JHTML.JHTML;
import JUOM.UniversalObjects.Universal;
import JUOM.UniversalObjects.UniversalException;
import JUOM.UniversalObjects.UniversalObject;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public abstract class ServerObject extends UniversalObject {

    protected ServerObject parent = null;

    protected record Resource(String resource, String mime) {}

    protected static Dictionary<String, String> extensionToMIME = new Hashtable<>();
    static {
        extensionToMIME.put("html", "text/html");
        extensionToMIME.put("css", "text/css");
        extensionToMIME.put("js", "application/javascript");
        extensionToMIME.put("png", "image/png");
        extensionToMIME.put("jpg", "image/jpeg");
        extensionToMIME.put("gif", "image/gif");
        extensionToMIME.put("ico", "image/x-icon");
    }

    protected final String nextURLPart(String url) {

        String[] parts = url.startsWith("?") ? url.split("/") : url.split("[/?]");
        return parts.length > 1 ? parts[1] : "";
    }

    protected final String truncateUrL(String url) {
        //System.out.println("Untruncated URL: " + url);
        if(url.charAt(url.length() - 1) != '/') {
            url += "/";
        }
        int q = url.indexOf("?");
        int s = url.indexOf("/");
        if(q > s) {
            url = '?'+ url.split("[/?]", 3)[2];
        } else {
            url = '/'+ url.split("[/?]", 3)[2];
        }

        if(url.charAt(url.length() - 1) != '/') {
            url += "/";
        }

        return url;
    }

    protected final String processUrl(String url) {

        if (url.equals("/")) {
            return "/";
        }

        List<String> splits = new LinkedList<>(Arrays.asList(url.split("/")));

        for(int i = 0; i < splits.size(); i++) {

            if(splits.get(i).equals("..")) {
                splits.remove(i);
                splits.remove(i - 1);
                i -= 2;
            } else if (splits.get(i).equals(".")) {
                splits.remove(i);
                i--;
            } else {
                splits.set(i, URLDecoder.decode(splits.get(i), StandardCharsets.UTF_8));
            }
        }
        return String.join("/", splits);
    }



    protected final void parseParams(HTTPServer.Client c, String paramString) throws IOException {

        try {

            System.out.println("Params: " + paramString);

            //paramString = URLDecoder.decode(paramString.substring(1, paramString.length() - 1), StandardCharsets.UTF_8);
            paramString = paramString.substring(1, paramString.length() - 1);

            //System.out.println("Params2: " + paramString);

            String[] paramsAndName = paramString.split("\\Q<&>\\E", 2);
            String methodName = paramsAndName[0];


            String[] params = paramsAndName.length > 1 ? paramsAndName[1].split("\\Q<&>\\E") : new String[0];

            if(params.length == 1 && params[0].isEmpty()) {
                params = new String[0];
            }

            Object[] objects = UniversalObject.parse(params);

            if(methodName.equals(this.getClass().getName())) {
                Constructor<?>[] constructors = this.getClass().getConstructors();
                for (Constructor<?> constructor : constructors) {
                    if(constructor.getParameterCount() == objects.length
                        && constructor.isAnnotationPresent(Universal.class)
                        && constructor.getAnnotation(Universal.class).webMethod()) {

                        try {
                            sendUniversalResponse(c, (UniversalObject) constructor.newInstance(objects));
                            return;
                        } catch (Exception ignored) {}
                    }
                }
            } else {
                for (Method method : this.getClass().getDeclaredMethods()) {
                    if (method.getName().equals(methodName)
                        && method.getParameterCount() == objects.length
                        && method.isAnnotationPresent(Universal.class)
                        && method.getAnnotation(Universal.class).webMethod()) {

                        try {
                            method.setAccessible(true);
                            sendUniversalResponse(c, UniversalObject.convert(method.invoke(this, objects)));
                            return;
                        } catch (Exception ignored) {}
                    }
                }
            }

            sendExceptionFoundResponse(c, "Method called " + paramsAndName[0] +  "() for page "
                    + this.getClass().getName() + " not found");

        } catch (Exception e) {
            sendExceptionFoundResponse(c, "Invalid parameters when executing page methods for page "
                    + this.getClass().getName() + ": " + e.getMessage());
        }
    }




    protected final Resource handleResource(String path) throws IOException {

        if(path.charAt(path.length() - 1) == '/') {
            path = path.substring(0, path.length() - 1);
        }

        System.out.println("Path: " + path);

        if(path.charAt(0) == '.') {
            throw new IOException("Invalid path");
        }

        path = path.substring(1);

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

            //System.out.println(path);

            InputStream is = this.getClass().getResourceAsStream(path);

            if (is == null) {
                return new Resource("Failed to load resource", extensionToMIME.get("json"));
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

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

    protected final void sendResponse(HTTPServer.Client c, Resource r, int code) throws IOException {
        c.out.write("HTTP/1.1 " + code + " OK\r\n" +
                "Content-Type: " + r.mime() + "\r\n" +
                "\r\n" +
                r.resource());
        c.out.flush();
    }

    protected final void sendPageNotFoundResponse(HTTPServer.Client c, String message) throws IOException {
        sendHTMLResponse(c, pageNotFound(message));
    }

    protected final void sendResourceResponse(HTTPServer.Client c, Resource resource) throws IOException {
        sendResponse(c, resource, 200);
    }

    protected final void sendHTMLResponse(HTTPServer.Client c, JHTML html) throws IOException {
        sendResponse(c, new Resource(html.html(), "text/html"), 200);
    }

    protected final void sendUniversalResponse(HTTPServer.Client c, UniversalObject obj) throws IOException {
        sendResponse(c, new Resource(obj.json(), "application/json"), 200);
    }

    protected final void sendExceptionFoundResponse(HTTPServer.Client c, String message) throws IOException {
        sendUniversalResponse(c, new UniversalException(message));
    }

    protected final void sendNoResponse(HTTPServer.Client c) throws IOException {
        sendResponse(c, new Resource("", "text/html"), 200);
    }



    protected abstract JHTML pageNotFound(String message);

    protected void handleURL(HTTPServer.Client c, String url) throws IOException {
        if(nextURLPart(url).isEmpty()) {
            parseParams(c, url);
        } else {
            try {
                sendResourceResponse(c, handleResource(url));
            } catch (IOException e) {
                sendPageNotFoundResponse(c, "File not found");
            }
        }
    }

    protected String path() {
        return this.parent.path() + this.getClass().getSimpleName()  + "/";
    }
}
