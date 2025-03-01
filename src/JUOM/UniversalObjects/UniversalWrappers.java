package JUOM.UniversalObjects;

public class UniversalWrappers {
    public static class UOString extends UniversalObject {
        @Universal
        public String value;

        public UOString(String value) {
            this.value = value;
        }
    }

    public static class UOChar extends UniversalObject {
        @Universal
        public char value;

        public UOChar(char value) {
            this.value = value;
        }
    }

    public static class UOInt extends UniversalObject {
        @Universal
        public int value;

        public UOInt(int value) {
            this.value = value;
        }
    }

    public static class UOBoolean extends UniversalObject {
        @Universal
        public boolean value;

        public UOBoolean(boolean value) {
            this.value = value;
        }
    }

    public static class UOArray extends UniversalObject {
        @Universal
        public UniversalObject[] value;

        public UOArray(UniversalObject[] value) {
            this.value = value;
        }
    }

    public static class UONull extends UniversalObject {
        public UONull() {}
    }


}
