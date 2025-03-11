package JUOM.UniversalObjects;

public class UniversalWrappers {
    public final static class UOString extends UniversalObject {
        @Universal
        public String value;

        public UOString(String value) {
            this.value = value;
        }
    }

    public final static class UOChar extends UniversalObject {
        @Universal
        public char value;

        public UOChar(char value) {
            this.value = value;
        }
    }

    public final static class UOInt extends UniversalObject {
        @Universal
        public int value;

        public UOInt(int value) {
            this.value = value;
        }
    }

    public final static class UOBoolean extends UniversalObject {
        @Universal
        public boolean value;

        public UOBoolean(boolean value) {
            this.value = value;
        }
    }

    public final static class UOArray extends UniversalObject {
        @Universal
        public UniversalObject[] value;

        public UOArray(UniversalObject[] value) {
            this.value = java.util.Arrays.copyOf(value, value.length);
        }
    }

    public final static class UONull extends UniversalObject {
        public UONull() {}
    }

    public final static class UOFloat extends UniversalObject {
        @Universal
        public float value;

        public UOFloat(float value) {
            this.value = value;
        }
    }

    public final static class UODouble extends UniversalObject {
        @Universal
        public double value;

        public UODouble(double value) {
            this.value = value;
        }
    }


}
