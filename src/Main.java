import JUOM.UniversalObjects.Universal;
import JUOM.UniversalObjects.UniversalObject;
import JUOM.UniversalObjects.UniversalWrappers;
import MyServer.MyServer;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws Exception {

        MyServer e = new MyServer(3001);

        e.start();

    }
}