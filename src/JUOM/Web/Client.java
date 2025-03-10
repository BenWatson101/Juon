package JUOM.Web;

import JUOM.JHTML.JHTML;
import JUOM.UniversalObjects.UniversalException;
import JUOM.UniversalObjects.UniversalObject;

import java.io.*;
import java.net.Socket;
import java.util.*;

public final class Client implements AutoCloseable {
    private final BufferedReader in;
    private final BufferedWriter out;
    private final Socket socket;
    private final Dictionary<String, String[]> headers = new Hashtable<>();
    private final Map<String, LinkedList<String>> responseHeaders = new Hashtable<>();

    private int responseCode = 200;
    private String responseMessage = "OK";
    private String content = "";


    public Client(Socket socket) throws IOException {
        this.socket = socket;
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

        String line;

        if(!(line = in.readLine()).isEmpty()) {
            String[] parts = line.split(" ");
            if (parts.length == 3) {
                headers.put("Method", new String[] {parts[0]});
                headers.put("URL", new String[] {parts[1]});
                headers.put("Version", new String[] {parts[2]});
            }
        }

        while (!(line = in.readLine()).isEmpty()) {
            String[] parts = line.split(":", 2);
            if (parts.length == 2) {
                String headerName = parts[0].trim();
                String headerValue = parts[1].trim();
                // Special handling for headers that shouldn't be split
                if (!headerName.equalsIgnoreCase("Set-Cookie") &&
                    !headerName.equalsIgnoreCase("Cookie")) {
                    headers.put(headerName, headerValue.split(","));
                } else {
                    headers.put(headerName, new String[]{headerValue});
                }
            }
        }
    }

    private void respond() throws IOException {
        out.write("HTTP/1.1 " + responseCode + " " + responseMessage + "\r\n");

        for (String header : responseHeaders.keySet()) {
            out.write(header + ": " + String.join(", ", responseHeaders.get(header)) + "\r\n");
        }

        out.write("\r\n");
        out.write(content);

        out.flush();
    }


    @Override
    public void close() throws IOException {

        respond();

        in.close();
        out.close();
        socket.close();
    }

    public String[] getHeader(String header) {
        return headers.get(header);
    }

    public String url() {
        return headers.get("URL")[0];
    }

    public void addResponseHeader(String header, String value) {
        LinkedList<String> values = responseHeaders.computeIfAbsent(header, k -> new LinkedList<>());
        values.add(value);
    }

    public void setResponseHeaders(String header, String value) {
        LinkedList<String> values = new LinkedList<>();
        values.add(value);
        responseHeaders.put(header, values);
    }

    public void setResponseCode(int code) {
        responseCode = code;
    }

    public void setResponseMessage(String message) {
        responseMessage = message;
    }

    public Hashtable<String, String> getCookies() {
        Hashtable<String, String> cookies = new Hashtable<>();
        String[] cookieHeaders = headers.get("Cookie");
        if (cookieHeaders != null) {
            String[] cookieParts = cookieHeaders[0].split(";");
            for (String cookiePart : cookieParts) {
                String[] cookie = cookiePart.split("=", 2);
                if (cookie.length == 2) {
                    cookies.put(cookie[0].trim(), cookie[1].trim());
                }
            }
        }
        return cookies;
    }

    public void setCookie(String name, String value) {
        String setCookieHeader = responseHeaders.get("Set-Cookie") == null ? "" : responseHeaders.get("Set-Cookie").get(0);
        setCookieHeader += name + "=" + value + "; ";
        responseHeaders.put("Set-Cookie", new LinkedList<>(List.of(setCookieHeader)));
    }

    public void addCookieType(String type) {
        String setCookieHeader = responseHeaders.get("Set-Cookie") == null ? "" : responseHeaders.get("Set-Cookie").get(0);
        setCookieHeader += type + "; ";
        responseHeaders.put("Set-Cookie", new LinkedList<>(List.of(setCookieHeader)));
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setResponse(JHTML jhtml) {
        setResponseHeaders("Content-Type", "text/html");
        setContent(jhtml.html());
    }

    public void setResponse(UniversalObject obj) {
        setResponseHeaders("Content-Type", "application/json");
        setContent(obj.json());
    }

    public void setResponse() {
        setResponseHeaders("Content-Type", "text/plain");
        setContent("");
    }

    public void setResponse(String content) {
        setResponseHeaders("Content-Type", "text/plain");
        setContent(content);
    }

    public void setResponse(UniversalException e) {
        setResponse((UniversalObject) e);
    }


}
