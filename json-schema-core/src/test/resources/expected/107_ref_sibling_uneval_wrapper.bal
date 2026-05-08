import ballerina/data.jsondata;

public type SchemaAllOf1Object record {|
    string bar?;
    json...;
|};

public type SchemaAllOf2Object record {|
    string foo?;
    json...;
|};

public type SchemaAllOf1 boolean|string|[json...]|SchemaAllOf1Object|()|int|float|decimal;

public type SchemaAllOf2 boolean|string|[json...]|SchemaAllOf2Object|()|int|float|decimal;

@jsondata:AllOf
public type SchemaSubTypes SchemaAllOf1|SchemaAllOf2;

@jsondata:UnevaluatedProperties {
    value: never
}
@jsondata:AllOf
public type Schema json|SchemaSubTypes;
