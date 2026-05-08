import ballerina/data.jsondata;

@jsondata:NumberConstraints {
    multipleOf: 2.0
}
public type SchemaOneOf1AllOf1 int;

@jsondata:NumberConstraints {
    multipleOf: 3.0
}
public type SchemaOneOf1AllOf2 int;

@jsondata:AllOf
public type SchemaOneOf1SubTypes SchemaOneOf1AllOf1|SchemaOneOf1AllOf2;

@jsondata:AllOf
public type SchemaOneOf1 int|SchemaOneOf1SubTypes;

@jsondata:NumberConstraints {
    multipleOf: 2.0
}
public type SchemaOneOf2AllOf1NotNumber int|float|decimal;

public type SchemaOneOf2AllOf1Not SchemaOneOf2AllOf1NotNumber|boolean|string|[json...]|record {|
    json...;
|}|();

@jsondata:Not {
    value: SchemaOneOf2AllOf1Not
}
public type SchemaOneOf2AllOf1 int;

@jsondata:NumberConstraints {
    multipleOf: 5.0
}
public type SchemaOneOf2AllOf2 int;

@jsondata:AllOf
public type SchemaOneOf2SubTypes SchemaOneOf2AllOf1|SchemaOneOf2AllOf2;

@jsondata:AllOf
public type SchemaOneOf2 int|SchemaOneOf2SubTypes;

@jsondata:OneOf
public type SchemaSubTypes SchemaOneOf1|SchemaOneOf2;

@jsondata:AllOf
public type Schema int|SchemaSubTypes;
