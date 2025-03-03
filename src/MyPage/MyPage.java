package MyPage;

import JUOM.JHTML.JHTML;
import JUOM.JHTML.JHTMLUtils;
import JUOM.Web.Page;

public class MyPage extends Page {

    @Override
    protected JHTML startingPage() {
        return JHTMLUtils.boilerplate("Hello, World!");
    }

    @Override
    protected JHTML pageNotFound(String message) {
        return JHTML.text(message);
    }


}
