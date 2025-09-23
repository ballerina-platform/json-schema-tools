import ballerina/data.jsondata;

@jsondata:ArrayConstraints {
    uniqueItems: true,
    contains: {contains: SchemaContains, minContains: 0}
}
public type Schema [string...];

@jsondata:NumberConstraints {
    multipleOf: 2.0
}
public type SchemaContains int;
