package MyPage;

import JUOM.JHTML.JHTML;
import JUOM.Web.Page;

public class MyPage extends Page {

    @Override
    protected JHTML startingPage() {
        return JHTML.text("<h1>My Page</h1>");
    }

    @Override
    protected JHTML pageNotFound(String message) {
        return JHTML.text(message);
    }


}
