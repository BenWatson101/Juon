package JUOM.JHTML;

import JUOM.UniversalObjects.Universal;
import JUOM.UniversalObjects.UniversalObject;
import JUOM.UniversalObjects.WrapMyselfUniversally;

import java.io.*;

public abstract class JHTML implements WrapMyselfUniversally {

    private JHTML first = null;
    private JHTML last = null;

    public static class JHTMLWrapper extends UniversalObject {
        @Universal
        private String content;

        public JHTMLWrapper(JHTML jhtml) {
            this.content = jhtml.html();
        }
    }

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

    public static JHTML template(JHTML... e) {
        return new JHTML() {
            @Override
            protected String htmlContent() {
                StringBuilder html = new StringBuilder();
                for(JHTML jhtml : e) {
                    html.append(jhtml.html());
                }
                return html.toString();
            }
        };
    }

    public static JHTML iff(boolean condition, JHTML jhtml) {
        return new JHTML() {
            @Override
            protected String htmlContent() {
                return condition ? jhtml.html() : "";
            }
        };
    }

    public static JHTML iff(boolean condition, JHTML jhtml, JHTML elseJHTML) {
        return new JHTML() {
            @Override
            protected String htmlContent() {
                return condition ? jhtml.html() : elseJHTML.html();
            }
        };
    }

    public static JHTML repeat(int times, JHTML jhtml) {
        return new JHTML() {
            @Override
            protected String htmlContent() {
                StringBuilder html = new StringBuilder();
                for(int i = 0; i < times; i++) {
                    html.append(jhtml.html());
                }
                return html.toString();
            }
        };
    }

    public abstract static class Counter {
        private int start;
        private int end;

        public Counter(int start, int end) {
            this.start = start;
            this.end = end;
        }

        public abstract JHTML execute(int i, JHTML jhtml);
    }

    public static JHTML forInt(Counter r , JHTML jhtml) {

        return new JHTML() {
            @Override
            protected String htmlContent() {
                StringBuilder html = new StringBuilder();
                for(int i = r.start; i < r.end; i++) {
                    html.append(r.execute(i, jhtml).html());
                }
                return html.toString();
            }
        };
    }


    @Override
    public final UniversalObject wrapMyself() {
        return new JHTMLWrapper(this);
    }



    protected abstract String htmlContent();
}
