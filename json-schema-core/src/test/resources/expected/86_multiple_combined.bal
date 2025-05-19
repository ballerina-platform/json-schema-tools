import ballerina/data.jsondata;

@jsondata:NumberConstraints {
    multipleOf: 5.0
}
public type SchemaAllOf1OneOf1 int;

@jsondata:NumberConstraints {
    multipleOf: 7.0
}
public type SchemaAllOf1OneOf2 int;

@jsondata:OneOf
public type SchemaAllOf1SubTypes SchemaAllOf1OneOf1|SchemaAllOf1OneOf2;

@jsondata:AllOf
public type SchemaAllOf1 int|SchemaAllOf1SubTypes;

@jsondata:NumberConstraints {
    multipleOf: 2.0
}
public type SchemaAllOf2AllOf1 int;

@jsondata:NumberConstraints {
    multipleOf: 3.0
}
public type SchemaAllOf2AllOf2 int;

@jsondata:AllOf
public type SchemaAllOf2SubTypes SchemaAllOf2AllOf1|SchemaAllOf2AllOf2;

@jsondata:AllOf
public type SchemaAllOf2 int|SchemaAllOf2SubTypes;

@jsondata:AllOf
public type SchemaSubTypes SchemaAllOf1|SchemaAllOf2;

@jsondata:AllOf
public type Schema int|SchemaSubTypes;
