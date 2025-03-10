package JUOM.Web;

import JUOM.WebServices.MonitoredThread;

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
                    new MonitoredThread(() -> {
                        try (Client c = new Client(clientSocket)) {
                            handleRequest(c, c.url());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }).start();
                } catch (IOException e) {
                    e.printStackTrace();
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

    protected void handleRequest(Client c, String url) throws IOException {

        if(!c.getHeader("Method")[0].equals("GET")) {
            return;
        }

        if(url.equals("/")) {
            c.setResponse(startingPage());
            return;
        }

//        System.out.println("Server URL: " + url);
//        System.out.println("Server next: " + nextURLPart(url));

        if(nextURLPart(url).equals(getClass().getSimpleName())) {
            url = truncateUrL(url);
        }

        ServerObject obj = serverObjectMap.get(nextURLPart(url));

        if(obj != null) {
            obj.handleURL(c, truncateUrL(url));

        } else {
            super.handleURL(c, url);
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
