package MyUniversalObject;


import JUOM.JHTML.JHTML;
import JUOM.Web.HTTPServer;
import JUOM.Web.ServerObject;

import java.io.IOException;

public class MyUniversalObject extends ServerObject {


    @Override
    protected JHTML pageNotFound(String message) {
        return null;
    }
}
