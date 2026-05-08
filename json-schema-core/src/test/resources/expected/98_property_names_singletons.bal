import ballerina/data.jsondata;

@jsondata:ObjectConstraints {
    propertyNames: SchemaObjectPropertyNames
}
public type SchemaObject record {|
    json...;
|};

public type SchemaObjectPropertyNames "foo"|"bar";

public type Schema boolean|string|[json...]|SchemaObject|()|int|float|decimal;
