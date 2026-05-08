import ballerina/data.jsondata;

public type SchemaMainTypeObject record {|
    string foo?;
    json...;
|};

public type SchemaMainType boolean|string|[json...]|SchemaMainTypeObject|()|int|float|decimal;

@jsondata:PatternProperties {
    value: [schemaAllOf1ObjectPatternElement1]
}
public type SchemaAllOf1Object record {|
    json...;
|};

jsondata:PatternPropertiesElement schemaAllOf1ObjectPatternElement1 = {
    pattern: re `^bar`,
    value: string
};

public type SchemaAllOf1 boolean|string|[json...]|SchemaAllOf1Object|()|int|float|decimal;

@jsondata:AllOf
public type SchemaSubTypes SchemaAllOf1;

@jsondata:UnevaluatedProperties {
    value: never
}
@jsondata:AllOf
public type Schema SchemaMainType|SchemaSubTypes;
