package MyPage;

import JUOM.JHTML.JHTML;
import JUOM.Web.Page;
import JUOM.JHTML.HTML.*;

public class MyPage extends Page {

    @Override
    protected JHTML startingPage() {

    }

    @Override
    protected JHTML pageNotFound(String message) {
        return JHTML.text(message);
    }


}
