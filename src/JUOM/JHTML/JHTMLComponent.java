package JUOM.JHTML;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;

public abstract class JHTMLComponent extends JHTML {

    LinkedList<String> classes = new LinkedList<>();
    String style = null;
    String ID = null;
    Dictionary<String, String> attributes = new Hashtable<>();

    LinkedList<String> misc = new LinkedList<>();

    String title = null;
    String lang = null;

    String src = null;

    public JHTMLComponent(String text) {
        this.child = new JHTMLText(text);
    }

    private class JHTMLText extends JHTML {
        private final String text;

        public JHTMLText(String text) {
            this.text = text;
        }

        @Override
        protected String htmlContent() {
            return text;
        }
    }


    @Override
    protected String htmlContent() {
        if(child == null) {
            return "<" + tag() + " " + content() + "/>";
        } else {
            return "<" + tag() + " " + content() + ">" + child.html() + "</" + tag() + ">";
        }
    }

    public JHTMLComponent classs(String classs) {
        classes.add(classs);
        return this;
    }

    public JHTMLComponent style(String style) {
        this.style = style;
        return this;
    }

    public JHTMLComponent ID(String ID) {
        this.ID = ID;
        return this;
    }

    public JHTMLComponent attribute(String key, String value) {
        attributes.put(key, value);
        return this;
    }

    public JHTMLComponent title(String title) {
        this.title = title;
        return this;
    }

    public JHTMLComponent lang(String lang) {
        this.lang = lang;
        return this;
    }

    public JHTMLComponent misc(String misc) {
        this.misc.add(misc);
        return this;
    }

    public JHTMLComponent src(String src) {
        this.src = src;
        return this;
    }

    private String content() {
        StringBuilder content = new StringBuilder();

        if(!classes.isEmpty()) {
            content.append("class=\"");
            for(String classs : classes) {
                content.append(classs).append(" ");
            }
            content.deleteCharAt(content.length() - 1);
            content.append("\" ");
        }

        if(style != null) {
            content.append("style=\"").append(style).append("\" ");
        }

        if(ID != null) {
            content.append("id=\"").append(ID).append("\" ");
        }

        Enumeration<String> keys = attributes.keys();
        while(keys.hasMoreElements()) {
            String key = keys.nextElement();
            content.append(key).append("=\"").append(attributes.get(key)).append("\" ");
        }

        if (title != null) {
            content.append("title=\"").append(title).append("\" ");
        }

        if (lang != null) {
            content.append("lang=\"").append(lang).append("\" ");
        }

        for(String m : misc) {
            content.append(m).append(" ");
        }

        return content.toString();
    }


    protected abstract String tag();
}
