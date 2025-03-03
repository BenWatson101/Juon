package JUOM.JHTML;

public class JHTMLUtils {
    public static JHTML boilerplate(JHTML jhtml) {
        return JHTML.template(
                JHTML.text(
                        "<!DOCTYPE html>" +
                        "<html lang=\"en\">" +
                        "<head>" +
                        "<meta charset=\"UTF-8\">" +
                        "<title>My Page</title>" +
                        "</head>" +
                        "<body>"
                ),

                jhtml,

                JHTML.text(
                        "</body>" +
                        "</html>"
                )
        );
    }

    public static JHTML boilerplate(String text) {
        return boilerplate(JHTML.text(text));
    }


}
