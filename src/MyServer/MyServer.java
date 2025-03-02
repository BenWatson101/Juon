package MyServer;

import JUOM.JHTML.JHTML;
import JUOM.Web.Server;

import java.io.IOException;

public class MyServer extends Server {

    public MyServer(int port) throws IOException {
        super(port);

        this.addServerObject(new MyPage.MyPage());
    }

    @Override
    protected JHTML startingPage() {
        try {
            return JHTML.file("index.html", this.getClass());
        } catch (IOException e) {
            return pageNotFound("Could not find index.html");
        }
    }

    @Override
    protected JHTML pageNotFound(String message) {
        return JHTML.text(message);
    }


}
