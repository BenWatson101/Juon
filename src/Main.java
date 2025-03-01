import JUOM.UniversalObjects.UniversalObject;
import JUOM.UniversalObjects.UniversalWrappers;

public class Main {
    public static void main(String[] args) {

        try {
            char f = (char)UniversalObject.parse(UniversalObject.convert('e').json());

            System.out.println(f);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
}