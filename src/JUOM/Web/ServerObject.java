package JUOM.Web;

import JUOM.JHTML.JHTML;
import JUOM.UniversalObjects.Universal;
import JUOM.UniversalObjects.UniversalException;
import JUOM.UniversalObjects.UniversalObject;
import JUOM.WebServices.FileManager;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static JUOM.Web.Resource.*;

public abstract class ServerObject extends UniversalObject {

    protected ServerObject parent = null;



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

            switch (splits.get(i)) {
                case ".." -> {
                    splits.remove(i);
                    splits.remove(i - 1);
                    i -= 2;
                }
                case "." -> {
                    splits.remove(i);
                    i--;
                }
                case "..." -> {
                    splits.clear();
                    splits.add("");
                    i = -1;
                }
                default -> splits.set(i, URLDecoder.decode(splits.get(i), StandardCharsets.UTF_8));
            }
        }
        return String.join("/", splits);
    }



    protected final void parseParams(Client c, String paramString) throws IOException {

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
                            c.setResponse((UniversalObject) constructor.newInstance(objects));
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
                            c.setResponse(UniversalObject.convert(method.invoke(this, objects)));
                            return;
                        } catch (Exception ignored) {}
                    }
                }
            }

            c.setResponse(new UniversalException("Method called " + paramsAndName[0] +  "() for page "
                    + this.getClass().getName() + " not found"));

        } catch (Exception e) {
            c.setResponse(new UniversalException("Invalid parameters when executing page methods for page "
                    + this.getClass().getName() + ": " + e.getMessage()));
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

            byte[] bytes = FileManager.readFile(path, this.getClass());

            if (bytes == null) {
                return new Resource("Failed to load content", extensionToMIME.get("json"));
            }

            return new Resource(new String(bytes, StandardCharsets.UTF_8), mime);

        } else {
            throw new IOException("No file extension");
        }
    }


    protected abstract JHTML objectOrResourceNotFound(String message);

    protected void handleURL(Client c, String url) throws IOException {
        if(nextURLPart(url).isEmpty()) {
            parseParams(c, url);
        } else {
            try {
                c.setResponse(handleResource(url));
            } catch (IOException e) {
                c.setResponseCode(404);
                c.setResponseMessage(e.getMessage());
                c.setResponse(objectOrResourceNotFound(e.getMessage()));
            }
        }
    }

    protected String path() {
        return this.parent.path() + this.getClass().getSimpleName()  + "/";
    }
}
