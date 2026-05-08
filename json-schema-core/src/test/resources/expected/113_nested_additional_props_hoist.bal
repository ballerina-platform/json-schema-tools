import ballerina/data.jsondata;

public type SchemaMainTypeObject record {|
    string foo?;
    json...;
|};

public type SchemaMainType boolean|string|[json...]|SchemaMainTypeObject|()|int|float|decimal;

@jsondata:AdditionalProperties {
    value: json
}
public type SchemaAllOf1Object record {|
    json...;
|};

public type SchemaAllOf1 boolean|string|[json...]|SchemaAllOf1Object|()|int|float|decimal;

@jsondata:AllOf
public type SchemaSubTypes SchemaAllOf1;

@jsondata:UnevaluatedProperties {
    value: never
}
@jsondata:AllOf
public type Schema SchemaMainType|SchemaSubTypes;
