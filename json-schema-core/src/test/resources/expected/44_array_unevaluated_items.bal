import ballerina/data.jsondata;

@jsondata:ArrayValidation {
    contains: {contains: SchemaContains, minContains: 3},
    unevaluatedItems: SchemaUnevaluatedItems
}
public type Schema [(int|float|decimal)...];

@jsondata:NumberValidation {
    multipleOf: 2.0
}
public type SchemaContainsNumber int|float|decimal;

type SchemaContains ()|boolean|string|record {|
    json...;
|}|[json...]|SchemaContainsNumber;

public type SchemaUnevaluatedItems int|float|decimal;
