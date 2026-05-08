import ballerina/data.jsondata;

@jsondata:ArrayConstraints {
    prefixItems: [SchemaItem0]
}
public type Schema [(SchemaItem0|string)...];

@jsondata:NumberConstraints {
    minimum: 4.0
}
public type SchemaItem0 int;
