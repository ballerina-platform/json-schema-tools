import ballerina/data.jsondata;

@jsondata:ArrayValidation {
    uniqueItems: true,
    contains: {contains: SchemaContains, minContains: 0}
}
public type Schema [string...];

@jsondata:NumberValidation {
    multipleOf: 2.0
}
public type SchemaContains int;
