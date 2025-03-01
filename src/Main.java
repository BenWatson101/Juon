import JUOM.UniversalObjects.UniversalObject;
import JUOM.UniversalObjects.UniversalWrappers;

public class Main {
    public static void main(String[] args) {

        try {
            char e = (char)UniversalObject.parse(UniversalObject.convert('e').json());

            System.out.println(e);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
}