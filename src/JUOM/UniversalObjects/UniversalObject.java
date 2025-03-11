package JUOM.UniversalObjects;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;

import JUOM.WebServices.FileManager;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

// Anything that extends this class can be handed to client as an object
// which can be used
public abstract class UniversalObject {

    public final String json() {
        StringBuilder json = new StringBuilder("{");
        Field[] fields = this.getClass().getDeclaredFields();
        boolean first = true;


        json.append("\"class\":\"").append(this.getClass().getName()).append("\", \"fields\":{");

        for (Field field : fields) {
            if (field.isAnnotationPresent(Universal.class)) {
                field.setAccessible(true);
                try {

                    if (!first) {
                        json.append(",");
                    }
                    json.append("\"").append(field.getName()).append("\":");
                    Object value = field.get(this);
                    if (value instanceof String || value instanceof Character) {
                        json.append("\"").append(value).append("\"");
                    } else if (value instanceof UniversalObject) {
                        json.append(((UniversalObject) value).json());
                    } else if (value.getClass().isArray()) {
                        json.append("[");
                        int length = java.lang.reflect.Array.getLength(value);
                        for (int i = 0; i < length; i++) {
                            Object arrayElement = java.lang.reflect.Array.get(value, i);
                            if (i > 0) {
                                json.append(",");
                            }
                            if (arrayElement instanceof String) {
                                json.append("\"").append(arrayElement).append("\"");
                            } else if (arrayElement instanceof UniversalObject) {
                                json.append(((UniversalObject) arrayElement).json());
                            } else {
                                json.append(arrayElement);
                            }
                        }
                        json.append("]");
                    } else {
                        json.append(value);
                    }
                    first = false;
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        json.append("}}");
        return json.toString();
    }


    //common format for a json:
    // {"class":"com.example.math.Calculator",
    // fields:{
    // "field1":"value1",
    // "field2":"value2",
    // "field3": {"class":"MyClass2", fields:{"field1":"value1"}}
    // "field4": [1,2,3,4]
    // }
    // }
    //common format for a json containing only a String, int or boolean:
    // {"class":"String",
    // "value":"Hello World"
    // }
    public static Object[] parse(String[] jsons) throws Exception {

        Object[] objects = new Object[jsons.length];
        for (int i = 0; i < jsons.length; i++) {
            objects[i] = parse(jsons[i]);
        }
        return objects;
    }

    public static Object parse(String json) throws Exception {
        JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
        String className = jsonObject.get("class").getAsString();

        JsonObject fields = jsonObject.getAsJsonObject("fields");

        if (className.equals(UniversalWrappers.UOString.class.getName())) {
            return fields.get("value").getAsString();
        } else if (className.equals(UniversalWrappers.UOInt.class.getName())) {
            return fields.get("value").getAsInt();
        } else if (className.equals(UniversalWrappers.UOBoolean.class.getName())) {
            return fields.get("value").getAsBoolean();
        } else if (className.equals(UniversalWrappers.UOChar.class.getName())) {
            return fields.get("value").getAsString().charAt(0);
        } else if (className.equals(UniversalWrappers.UOArray.class.getName())) {
            UniversalObject[] array = new UniversalObject[fields.getAsJsonArray("value").size()];
            for (int i = 0; i < array.length; i++) {
                array[i] = (UniversalObject) parse(fields.getAsJsonArray("value").get(i).toString());
            }
            return array;
        } else if (className.equals(UniversalWrappers.UONull.class.getName())) {
            return null;
        } else if (className.equals(UniversalWrappers.UOFloat.class.getName())) {
            return fields.get("value").getAsFloat();
        } else if (className.equals(UniversalWrappers.UODouble.class.getName())) {
            return fields.get("value").getAsDouble();
        }



        Class<?> clazz = Class.forName(className);
        Object instance = clazz.getDeclaredConstructor().newInstance();


        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Universal.class)) {
                field.setAccessible(true);
                if (fields.has(field.getName())) {
                    Object value = new Gson().fromJson(fields.get(field.getName()), field.getType());
                    field.set(instance, value);
                }
            }
        }
        return instance;
    }

    public static UniversalObject convert(Object o) throws IllegalArgumentException {
        if(o instanceof UniversalObject uo) {
            return uo;
        } else if (o instanceof WrapMyselfUniversally) {
            return ((WrapMyselfUniversally) o).wrapMyself();
        } else if(o == null) {
            return new UniversalWrappers.UONull();
        } else if (o instanceof String) {
            return new UniversalWrappers.UOString((String) o);
        } else if (o instanceof Character) {
            return new UniversalWrappers.UOChar((char) o);
        } else if (o instanceof Integer) {
            return new UniversalWrappers.UOInt((int) o);
        } else if (o instanceof Boolean) {
            return new UniversalWrappers.UOBoolean((boolean) o);
        } else if (o.getClass().isArray()) {
            UniversalObject[] array = new UniversalObject[java.lang.reflect.Array.getLength(o)];
            for (int i = 0; i < array.length; i++) {
                array[i] = convert(java.lang.reflect.Array.get(o, i));
            }
            return new UniversalWrappers.UOArray(array);
        } else if (o instanceof Float) {
            return new UniversalWrappers.UOFloat((float) o);
        } else if (o instanceof Double) {
            return new UniversalWrappers.UODouble((double) o);
        } else {
            throw new IllegalArgumentException("Object is not a UniversalObject: " + o.getClass().getName());
        }
    }

    // Deep copy method
    public UniversalObject deepCopy() {
        try {
            String json = this.json();
            return (UniversalObject) parse(json);
        } catch (Exception e) {
            throw new RuntimeException("Failed to copy UniversalObject", e);
        }
    }

    // Equals method
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        UniversalObject that = (UniversalObject) obj;
        return this.json().equals(that.json());
    }

    // HashCode method
    @Override
    public int hashCode() {
        return this.json().hashCode();
    }

    // ToString method
    @Override
    public String toString() {
        return this.json();
    }

    protected void buildJavascript() throws IOException {
        //names
        String className = this.getClass().getName();
        String simpleName = this.getClass().getSimpleName();

        //fields
        Field[] fields = this.getClass().getDeclaredFields();

        //methods
        ArrayList<Method> UniversalMethods = new ArrayList<>();

        //populate methods
        for(Method method : this.getClass().getDeclaredMethods()) {
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
                prototypeFile.toString().getBytes(), this.getClass());
    }

}
