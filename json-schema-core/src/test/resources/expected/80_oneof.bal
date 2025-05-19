import ballerina/data.jsondata;

@jsondata:NumberConstraints {
    multipleOf: 2.0
}
public type SchemaOneOf1 int;

@jsondata:NumberConstraints {
    multipleOf: 3.0
}
public type SchemaOneOf2 int;

@jsondata:OneOf
public type SchemaSubTypes SchemaOneOf1|SchemaOneOf2;

@jsondata:AllOf
public type Schema int|SchemaSubTypes;
