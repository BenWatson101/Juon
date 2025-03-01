package MyServer;

import JUOM.JHTML.JHTML;
import JUOM.Web.Server;

import java.io.IOException;

public class MyServer extends Server {
    public MyServer(int port) throws IOException {
        super(port);
    }

    @Override
    protected JHTML startingPage() {
        try {
            return JHTML.file("start.html", this.getClass());
        } catch (IOException e) {
            return JHTML.text("<h1>ERROR</h1><p>error loading fileeee</p>");
        }
    }

    @Override
    protected JHTML pageNotFound(String message) {
        try {
            return JHTML.file("error.html", this.getClass());
        } catch (IOException e) {
            return JHTML.text("<h1>ERROR</h1><p>error loading file</p>");
        }
    }


}
