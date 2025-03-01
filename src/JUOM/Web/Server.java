package JUOM.Web;

import JUOM.JHTML.JHTML;
import JUOM.UniversalObjects.UniversalObject;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public abstract class Server extends Page {

    private final int port;
    private final ServerSocket serverSocket;
    private boolean running = true;

    private final Map<String, Page> pageMap = new HashMap<>();

    public Server(int port) throws IOException {
        this.port = port;
        this.serverSocket = new ServerSocket(port);
    }

    public void start() {
        running = true;

        new Thread(() -> {
            while (running) {
                new Thread(this::attemptConnection).start();
            }
        }).start();
    }

    public void stop() {
        running = false;
    }

    private class Client implements AutoCloseable {
        public final BufferedReader in;
        public final BufferedWriter out;

        public Client() throws IOException {
            Socket socket = serverSocket.accept();
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        }

        public void close() throws IOException {
            in.close();
            out.close();
        }
    }

    private void attemptConnection() {

        try (Client client = new Client()) {

            String line;
            while (!(line = client.in.readLine()).isEmpty()) {

                //expecting something like: GET /index HTTP/1.1
                String[] requestParts = line.split(" ");
                if (requestParts.length >= 2) {
                    handleRequest(client, requestParts[0], requestParts[1], line);
                } else {
                    sendNotFoundResponse(client, "Incorrect request format\n http request: " + line);
                }
            }

        } catch (IOException e) {
            System.out.println("Client connection error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleRequest(Client client, String method, String path, String line) {
        try {

            if(path.charAt(0) != '/' || !method.equals("GET")) {
                sendNotFoundResponse(client, "All requests must be GET requests to a path starting with /");
                return;
            }

            if (path.equals("/")) {
                sendHTMLResponse(client, startingPage());

            } else {
                //expecting something like: /index?method<\?>param1<\?>param2
                if(path.contains("?")) {
                    String[] pathParts = path.split("\\?", 2);
                    String pagePath = pathParts[0];

                    Page page = pageMap.get(pagePath.substring(1));

                    if (page == null) {
                        sendNotFoundResponse(client, "Page not found");
                        return;
                    }

                    if(pathParts.length == 1) {
                        sendHTMLResponse(client, page.startingPage());
                    } else {
                        parseParams(client, pathParts[1], page, line);
                    }
                //expecting something like: /index/something.extension
                //or /index/something/somethingElse.extension
                //or /something.extension
                } else {
                    String[] splits = path.split("/",3);
                    Page page = pageMap.get(splits[1]);
                    try {
                        if(page != null) {
                            sendResourceResponse(client, page.handleResource(splits[2]));
                        } else {
                            sendResourceResponse(client, handleResource(path.split("/",2)[1]));
                        }
                    } catch (Exception e) {
                        sendNotFoundResponse(client, "Error handling request: " + e.getMessage()
                                + "\n http request: " + line);
                    }
                }
            }

        } catch (Exception e) {
            System.out.println("Error handling request: " + e.getMessage() + "\n http request: " + line);
            e.printStackTrace();
        }
    }

    private void parseParams(Client c, String paramString, Page p, String line) throws Exception {
        String[] paramsAndName = paramString.split("<\\?>", 2);
        String methodName = paramsAndName[0];
        String[] params = paramsAndName[1].split("<\\?>");
        Object[] objects = UniversalObject.parse(params);

        if(methodName.equals(p.getClass().getName())) {
            Constructor<?>[] constructors = p.getClass().getConstructors();
            for (Constructor<?> constructor : constructors) {
                if(constructor.getParameterCount() == objects.length) {

                    sendUniversalResponse(c, (UniversalObject) constructor.newInstance(objects));
                    return;
                }
            }
        } else {
            for (Method method : p.getClass().getDeclaredMethods()) {
                if (method.getParameterCount() == objects.length) {
                    method.setAccessible(true);

                    sendUniversalResponse(c, UniversalObject.convert(method.invoke(p, objects)));
                    return;
                }
            }
        }

        sendNotFoundResponse(c, "Method for page " + p.getClass().getName() + "not found" +
                "\n http request: " + line);

    }


    private void sendNotFoundResponse(Client c) throws IOException {
        sendNotFoundResponse(c, "");
    }

    private void sendNotFoundResponse(Client c, String message) throws IOException {
        String httpResponse = "HTTP/1.1 404 Not Found\r\n" +
                "Content-Type: text/html\r\n" +
                "\r\n" +
                pageNotFound(message).html();
        c.out.write(httpResponse);
        c.out.flush();
    }

    private void sendHTMLResponse(Client c, JHTML html) throws IOException {
        String httpResponse = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: text/html\r\n" +
                "\r\n" +
                html.html();
        c.out.write(httpResponse);
        c.out.flush();
    }

    private void sendUniversalResponse(Client c, UniversalObject obj) throws IOException {
        String httpResponse = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: application/json\r\n" +
                "\r\n" +
                "{\"class\":" + obj.getClass().getName() +
                ",\"contents\":" + obj.json() + "}";
        c.out.write(httpResponse);
        c.out.flush();
    }

    private void sendResourceResponse(Client c, Resource resource) throws IOException {
        String httpResponse = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: " + resource.mime() + "\r\n" +
                "\r\n" +
                resource.resource();
        c.out.write(httpResponse);
        c.out.flush();
    }

    public void addPage(String path, Page page) {
        pageMap.put(path, page);
    }
}
