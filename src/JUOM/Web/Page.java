package JUOM.Web;

import java.io.*;

import JUOM.JHTML.JHTML;
import JUOM.UniversalObjects.UniversalObject;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.rmi.server.ExportException;
import java.util.Arrays;
import java.net.URLDecoder;

public abstract class Page extends ServerObject {

    protected final String nextURLPart(String url) {
        String[] parts = url.split("[/?]");
        return parts.length > 1 ? parts[1] : "";
    }

    protected final String truncateUrL(String url) {
        return url.substring(url.indexOf("/") + 1);
    }

    protected abstract JHTML startingPage();



    @Override
    protected void handleURL(Server.Client c, String url) throws IOException {
        if(nextURLPart(url).isEmpty()) {
            parseParams(c, url.substring(1));
        } else {
            try {
                sendResourceResponse(c, handleResource(url));
            } catch (IOException e) {
                sendNotFoundResponse(c, "File not found");
            }
        }
    }


}
