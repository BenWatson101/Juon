package JUOM.Web;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public abstract class HTTPServer extends Page {

    private final int port;
    private final ServerSocket serverSocket;
    private boolean running = true;

    private final Map<String, ServerObject> serverObjectMap = new HashMap<>();

    private String domainName;

    public HTTPServer(int port) throws IOException {
        this.port = port;
        this.serverSocket = new ServerSocket(port);
        this.domainName = "http://localhost:" + port;
    }

    public final void start() {
        running = true;

        new MonitoredThread(() -> {
            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    new MonitoredThread(() -> handleClient(clientSocket)).start();
                } catch (IOException e) {
                    System.out.println("Accept failed: " + e.getMessage());
                }
            }
        }).start();

        new MonitoredThread(() -> {
            while (running) {
                try {
                    Thread.sleep(5000);
                    MonitoredThread.printInstancesAndMemory();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void stop() {
        running = false;
    }

    protected final class Client implements AutoCloseable {
        public final BufferedReader in;
        public final BufferedWriter out;
        private final Socket socket;

        public Client(Socket socket) throws IOException {
            this.socket = socket;
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        }

        @Override
        public void close() throws IOException {
            in.close();
            out.close();
            socket.close();
        }
    }

    private void handleClient(Socket clientSocket) {
        try (Client client = new Client(clientSocket)) {

            String line;
            while (!(line = client.in.readLine()).isEmpty()) {

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

        ServerObject obj = serverObjectMap.get(nextURLPart(url));

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

    //domain name WITHOUT the slash at the end
    @Override
    protected String path() {
        return domainName + "/";
    }

    public final void addServerObject(ServerObject obj) {
        serverObjectMap.put(obj.getClass().getSimpleName(), obj);
        obj.parent = this;
    }

    public final void setDomainName(String domainName) {
        this.domainName = domainName;
    }


}
