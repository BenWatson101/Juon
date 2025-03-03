package MyPage;

import JUOM.JHTML.JHTML;
import JUOM.Web.Page;

public class MyPage extends Page {

    @Override
    protected JHTML startingPage() {
        return JHTML.template(
            JHTML.text(
                    "<!DOCTYPE html>" +
                    "<html lang=\"en\">" +
                    "<head>" +
                    "<meta charset=\"UTF-8\">" +
                    "<title>My Page</title>" +
                    "</head>" +
                    "<body>" +
                    "<h1>Welcome to My Page</h1>" +
                    "</body>" +
                    "</html>"
            )

        );

    }

    @Override
    protected JHTML pageNotFound(String message) {
        return JHTML.text(message);
    }


}
