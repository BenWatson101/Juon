//goes from java class string to class
const UniversalMap = new Map();

//goes from string js primitive class name to wrapping function
//mainly meant for primitives
const WrappingMap = new Map();

//goes from string to unwrapping function
//mainly meant for primitives
const UnWrappingMap = new Map();

export default class UniversalObject {
    constructor() {}

    static registerClass(javaClass, jsclass) {
        UniversalMap.set(javaClass, jsclass);
    }
}


export function wrap(obj) {
    if (obj instanceof UniversalObject) {
        return obj;
    } else if (obj == null) {
        return new UniversalNull();
    } else if (WrappingMap.has(typeof obj)) {
        WrappingMap.get(typeof obj)(obj);
    } else {
        throw new Error('Object is not a UniversalObject');
    }
}

export function unwrap(json) {
    if(
        json == null || typeof json !== 'object'
        || !json.hasOwnProperty('class') || !json.hasOwnProperty('fields')
        || !UniversalMap.has(json.class)
    ) {
        throw new Error('Object is not a UniversalObject');
    }



    let obj = Object.create(UniversalMap.get(json.class).prototype);

    for (let key in json.fields) {
        obj[key] = json.fields[key];
    }

    //checks if it can be further unwrapped, for primitives
    if(UnWrappingMap.has(json.class)) {
        return UnWrappingMap.get(json.class)(obj);
    }

    return obj;
}

//--------------------Universal Wrappers--------------------
//
// These are the classes that are used to wrap the primitive types
// I am going fucking crazy!!!!!!!!!!
//
//--------------------Universal Wrappers--------------------

class UniversalString extends UniversalObject {
    constructor(value) {
        super();
        this.value = value;
    }
}
UniversalObject.registerClass('JUOM.UniversalObjects.UniversalWrappers$UOString', UniversalString);
UnWrappingMap.set('JUOM.UniversalObjects.UniversalWrappers$UOString', (obj) => obj.value);
WrappingMap.set('string', (value) => new UniversalString(value));

class UniversalChar extends UniversalObject {
    constructor(value) {
        super();
        this.value = value;
    }
}
UniversalObject.registerClass('JUOM.UniversalObjects.UniversalWrappers$UOChar', UniversalChar);
UnWrappingMap.set('JUOM.UniversalObjects.UniversalWrappers$UOChar', (obj) => obj.value);
WrappingMap.set('string', (value) => new UniversalChar(value));


class UniversalInt extends UniversalObject {
    constructor(value) {
        super();
        this.value = value;
    }
}
UniversalObject.registerClass('JUOM.UniversalObjects.UniversalWrappers$UOInt', UniversalInt);
UnWrappingMap.set('JUOM.UniversalObjects.UniversalWrappers$UOInt', (obj) => obj.value);
WrappingMap.set('number', (value) => new UniversalInt(value));

class UniversalBoolean extends UniversalObject {
    constructor(value) {
        super();
        this.value = value;
    }
}
UniversalObject.registerClass('JUOM.UniversalObjects.UniversalWrappers$UOBoolean', UniversalBoolean);
UnWrappingMap.set('JUOM.UniversalObjects.UniversalWrappers$UOBoolean', (obj) => obj.value);
WrappingMap.set('boolean', (value) => new UniversalBoolean(value));

class UniversalArray extends UniversalObject {
    constructor(value) {
        super();
        this.value = value;
    }
}
UniversalObject.registerClass('JUOM.UniversalObjects.UniversalWrappers$UOArray', UniversalArray);
UnWrappingMap.set('JUOM.UniversalObjects.UniversalWrappers$UOArray', (obj) => obj.value);
WrappingMap.set('array', (value) => new UniversalArray(value));

class UniversalNull extends UniversalObject {
    constructor() {
        super();
    }
}
UniversalObject.registerClass('JUOM.UniversalObjects.UniversalWrappers$UONull', UniversalNull);
UnWrappingMap.set('JUOM.UniversalObjects.UniversalWrappers$UONull', () => null);
//Wrapping is already handled by the wrap function

class UniversalDouble extends UniversalObject {
    constructor(value) {
        super();
        this.value = value;
    }
}
UniversalObject.registerClass('JUOM.UniversalObjects.UniversalWrappers$UODouble', UniversalDouble);
UnWrappingMap.set('JUOM.UniversalObjects.UniversalWrappers$UODouble', (obj) => obj.value);
WrappingMap.set('number', (value) => new UniversalDouble(value));

class UniversalFloat extends UniversalObject {
    constructor(value) {
        super();
        this.value = value;
    }
}
UniversalObject.registerClass('JUOM.UniversalObjects.UniversalWrappers$UOFloat', UniversalFloat);
UnWrappingMap.set('JUOM.UniversalObjects.UniversalWrappers$UOFloat', (obj) => obj.value);
WrappingMap.set('number', (value) => new UniversalFloat(value));

