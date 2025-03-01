package JUOM.JHTML;

import java.io.*;
import java.util.Objects;

public abstract class JHTML {

    private JHTML first = null;
    private JHTML last = null;

    JHTML child = null;

    public String html() {
        StringBuilder html = new StringBuilder();
        JHTML current = this;
        while(current != null) {
            html.append(current.htmlContent());
            current = current.first;
        }
        return html.toString();
    }

    public JHTML link(JHTML jhtml) {
        if(this.first == null) {
            this.first = jhtml;
        } else {
            this.last.first = jhtml;
        }
        this.last = jhtml;

        return this;
    }

    public JHTML child(JHTML jhtml) {
        this.child = jhtml;
        return this;
    }


    public static JHTML file(String path, Class<?> clazz) throws IOException {

        StringBuilder html = new StringBuilder();

//        System.out.println(path);
//        System.out.println("Absolute path: " + clazz.getResourceAsStream("/" + path));
//        System.out.println("Relative path: " + clazz.getResourceAsStream(path));;
//        System.out.println(clazz);

        InputStream e = clazz.getResourceAsStream(path);

        if (e == null) {
            throw new IOException("File not found");
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(e));


        String line;
        while((line = reader.readLine()) != null) {
            html.append(line).append("\n");
        }
        reader.close();

        return new JHTML() {
            @Override
            protected String htmlContent()  {
                return html.toString();
            }
        };
    }

    public static JHTML text(String text) {
        return new JHTML() {
            @Override
            protected String htmlContent() {
                return text;
            }
        };
    }




    protected abstract String htmlContent();
}
