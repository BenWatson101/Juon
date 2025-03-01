package JUOM.JHTML;

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

    protected abstract String htmlContent();
}
