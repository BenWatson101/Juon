package JUOM.Web;

import java.io.*;

import JUOM.JHTML.JHTML;

public abstract class Page extends ServerObject {




    protected abstract JHTML startingPage();



    @Override
    protected void handleURL(Client c, String url) throws IOException {

        if(url.equals("/")) {
            c.setResponse(startingPage());
        } else {
            super.handleURL(c, url);
        }
    }


}
