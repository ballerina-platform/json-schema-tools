import ballerina/data.jsondata;

@jsondata:NumberConstraints {
    multipleOf: 2.0
}
public type SchemaAllOf1 int;

@jsondata:NumberConstraints {
    multipleOf: 3.0
}
public type SchemaAllOf2 int;

@jsondata:AllOf
public type SchemaSubTypes SchemaAllOf1|SchemaAllOf2;

@jsondata:AllOf
public type Schema int|SchemaSubTypes;
