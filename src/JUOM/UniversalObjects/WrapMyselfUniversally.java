package JUOM.UniversalObjects;

// This interface is used to wrap an object into a UniversalObject
// This is useful when you want to wrap an object that is not a UniversalObject
// The front-end can't send this object back, and if they do, it won't be registered and the server will throw an error
public interface WrapMyselfUniversally {
    public UniversalObject wrapMyself();
}
