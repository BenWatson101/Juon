package JUOM.UniversalObjects;

import JUOM.WebServices.FileManager;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;

// This interface is used to wrap an object into a UniversalObject
// This is useful when you want to wrap an object that is not a UniversalObject
// The front-end can't send this object back, and if they do, it won't be registered and the server will throw an error
public interface WrapMyselfUniversally {
    public UniversalObject wrapMyself();

    public static <T extends WrapMyselfUniversally> void buildJavaScript(Class<T> clazz) throws IOException {
        //names
        String className = clazz.getName();
        String simpleName = clazz.getSimpleName();

        //fields
        Field[] fields = clazz.getDeclaredFields();

        //methods
        ArrayList<Method> UniversalMethods = new ArrayList<>();

        //populate methods
        for(Method method : clazz.getDeclaredMethods()) {
            if(method.isAnnotationPresent(Universal.class)) {
                UniversalMethods.add(method);
            }
        }

        //build javascript
        StringBuilder prototypeFile = new StringBuilder();

        prototypeFile.append("//-------------------------------\n");
        prototypeFile.append("//------------WARNING------------\n");
        prototypeFile.append("//--DO NOT PUT ANYTHING IN THIS--\n");
        prototypeFile.append("//--FILE AS IT WILL BE DELETED!--\n");
        prototypeFile.append("//-------------------------------\n");
        prototypeFile.append("//\n");
        prototypeFile.append("//-------------------------------\n");
        prototypeFile.append("//Built using JUOM version " + JUOM.JUOM.VERSION + "\n");
        prototypeFile.append("//-------------------------------\n");
        prototypeFile.append("//\n");
        prototypeFile.append("import UniversalObject from \"../Assets/UniversalObject\";\n\n\n\n");
        prototypeFile.append("export default class ").append(simpleName).append(" extends UniversalObject {}\n\n");

        prototypeFile.append("//Declaring fields\n");
        for(Field field : fields) {
            prototypeFile.append(simpleName).append(".prototype.").append(field.getName()).append(" = null;\n");
        }
        prototypeFile.append("\n\n\n\n\n");

        prototypeFile.append("//Declaring methods\n\n");
        for(Method method: UniversalMethods) {
            prototypeFile.append(simpleName).append(".prototype.").append(method.getName()).append(" = function(");
            prototypeFile.append(Arrays.stream(method.getParameters()).map(Parameter::getName).reduce((a, b) -> a + ", " + b).orElse(""));
            prototypeFile.append(") {\n");
            prototypeFile.append("    throw new Error(\"Method not implemented.\")\n");
            prototypeFile.append("};\n");

        }
        prototypeFile.append("\n\n\n");

        FileManager.writeFile(
                simpleName + "_.js",
                prototypeFile.toString().getBytes(), clazz);
    }

}
