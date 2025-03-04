package JUOM.Web;

import java.io.*;

import JUOM.JHTML.JHTML;

public abstract class Page extends ServerObject {

    protected final String nextURLPart(String url) {

        String[] parts = url.startsWith("?") ? url.split("/") : url.split("[/?]");
        return parts.length > 1 ? parts[1] : "";
    }

    protected final String truncateUrL(String url) {
        //System.out.println("Untruncated URL: " + url);
        if(url.charAt(url.length() - 1) != '/') {
            url += "/";
        }
        int q = url.indexOf("?");
        int s = url.indexOf("/");
        if(q > s) {
            url = '?'+ url.split("[/?]", 3)[2];
        } else {
            url = '/'+ url.split("[/?]", 3)[2];
        }

        if(url.charAt(url.length() - 1) != '/') {
            url += "/";
        }

        return url;
    }


    protected abstract JHTML startingPage();



    @Override
    protected void handleURL(HTTPServer.Client c, String url) throws IOException {

//        System.out.println("Page URL: " + url);
//        System.out.println("Page next: " + nextURLPart(url));

        if(url.equals("/")) {
            sendHTMLResponse(c, startingPage());
        } else if(nextURLPart(url).isEmpty()) {
            parseParams(c, url);
        } else {
            try {
                sendResourceResponse(c, handleResource(url));
            } catch (IOException e) {
                sendPageNotFoundResponse(c, "File not found");
            }
        }
    }


}
