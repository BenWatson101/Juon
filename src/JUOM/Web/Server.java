package JUOM.Web;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public abstract class Server extends Page {

    private final int port;
    private final ServerSocket serverSocket;
    private boolean running = true;

    private final Map<String, ServerObject> objectMap = new HashMap<>();

    public Server(int port) throws IOException {
        this.port = port;
        this.serverSocket = new ServerSocket(port);
    }

    public final void start() {
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

    protected final class Client implements AutoCloseable {
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

                    String url = requestParts[1];
                    String method = requestParts[0];

                    if (url.equals("/") && method.equals("GET")) {
                        sendHTMLResponse(client, startingPage());

                    } else {
                        handleRequest(client, requestParts[0], url);
                    }

                } else {
                    sendPageNotFoundResponse(client, "Incorrect request format\n http request: " + line);
                }
            }

        } catch (IOException e) {
            System.out.println("Client connection error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    protected void handleRequest(Client client, String method, String url) throws IOException {

        if(!method.equals("GET")) {
            return;
        }

//        System.out.println("Server URL: " + url);
//        System.out.println("Server next: " + nextURLPart(url));

        ServerObject obj = objectMap.get(nextURLPart(url));

        if(obj != null) {
            obj.handleURL(client, truncateUrL(url));

        } else if(url.contains("?")) {
            handleURL(client, url);

        } else {

            try {
                sendResourceResponse(client, handleResource(url));
            } catch (IOException e) {
                sendPageNotFoundResponse(client, "File not found");
            }
        }
    }

    public final void addServerObject(ServerObject obj) {
        objectMap.put(obj.getClass().getSimpleName(), obj);
    }
}
