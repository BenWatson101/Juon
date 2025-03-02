package MyServer;

import JUOM.JHTML.HTML;
import JUOM.JHTML.JHTML;
import JUOM.UniversalObjects.Universal;
import JUOM.Web.Server;

import java.io.IOException;

public class MyServer extends Server {

    public MyServer(int port) throws IOException {
        super(port);

        this.addServerObject(new MyPage.MyPage());
    }

    @Override
    protected JHTML startingPage() {
        return new HTML.Body().child(
                new HTML.H1().child(
                        JHTML.text("Hello World!").link(
                        new HTML.P().child(
                                JHTML.text("This is a test page.")
                        )
                        )
                )
        );
    }

    @Override
    protected JHTML pageNotFound(String message) {
        return JHTML.text(message);
    }


    void something() {
        System.out.println("Hello World!");
    }


}
