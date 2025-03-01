package JUOM.JHTML;

import java.io.*;

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


    public static JHTML file(String path) throws IOException {

        StringBuilder html = new StringBuilder();

        BufferedReader reader = new BufferedReader(new FileReader(path));

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
