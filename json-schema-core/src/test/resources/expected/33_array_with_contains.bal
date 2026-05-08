import ballerina/data.jsondata;

@jsondata:ArrayConstraints {
    uniqueItems: true,
    contains: {value: SchemaContains, minContains: 3, maxContains: 5}
}
public type Schema [string...];

@jsondata:NumberConstraints {
    multipleOf: 2.0
}
public type SchemaContains int;
