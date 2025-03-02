package JUOM.UniversalObjects;

import java.lang.reflect.Field;
import java.util.Arrays;

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
        } else {
            throw new IllegalArgumentException("Object is not a UniversalObject: " + o.getClass().getName());
        }
    }

}
