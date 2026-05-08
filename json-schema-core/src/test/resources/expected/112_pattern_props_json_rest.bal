import ballerina/data.jsondata;

@jsondata:UnevaluatedProperties {
    value: never
}
@jsondata:PatternProperties {
    value: [schemaObjectPatternElement1]
}
public type SchemaObject record {|
    json...;
|};

jsondata:PatternPropertiesElement schemaObjectPatternElement1 = {
    pattern: re `^foo`,
    value: string
};

public type Schema boolean|string|[json...]|SchemaObject|()|int|float|decimal;
