import ballerina/data.jsondata;

@jsondata:NumberConstraints {
    multipleOf: 2.0
}
public type SchemaAnyOf1 int;

@jsondata:NumberConstraints {
    multipleOf: 3.0
}
public type SchemaAnyOf2 int;

public type SchemaSubTypes SchemaAnyOf1|SchemaAnyOf2;

@jsondata:AllOf
public type Schema int|SchemaSubTypes;
