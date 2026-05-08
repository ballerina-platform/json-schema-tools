import ballerina/data.jsondata;

@jsondata:ArrayConstraints {
    prefixItems: [SchemaItem0]
}
public type Schema [(SchemaItem0|SchemaRestItem)...];

public type SchemaItem0 int|float|decimal;

public type SchemaRestItem string|int;
