import ballerina/data.jsondata;

@jsondata:UnevaluatedProperties {
    value: never
}
public type SchemaObject record {|
    @jsondata:DependentSchema {
        value: FooDependentSchema
    }
    string foo?;
    json...;
|};

public type FooDependentSchemaObject record {|
    "bar" bar;
    json...;
|};

public type FooDependentSchema boolean|string|[json...]|FooDependentSchemaObject|()|int|float|decimal;

public type Schema boolean|string|[json...]|SchemaObject|()|int|float|decimal;
