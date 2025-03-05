//goes from string to prototype
const UniversalMap = new Map();

//goes from string to wrapping function
//mainly meant for primitives
const WrappingMap = new Map();

//goes from string to unwrapping function
//mainly meant for primitives
const UnWrappingMap = new Map();

export default class UniversalObject {
    constructor() {
    }
}


function wrap(obj) {
    if (obj instanceof UniversalObject) {
        return obj;
    } else if (UniversalMap.has(typeof obj)) {

    } else {
        throw new Error('Object is not a UniversalObject');
    }
}

function unwrap(json) {
    if(
        json == null || typeof json !== 'object'
        || !json.hasOwnProperty('class') || !json.hasOwnProperty('fields')
        || !UniversalMap.has(json.class)
    ) {
        throw new Error('Object is not a UniversalObject');
    }

    let obj = Object.create(UniversalMap.get(json.class));

    for (let key in json.fields) {
        obj[key] = json.fields[key];
    }

    return obj;
}


