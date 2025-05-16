import ballerina/data.jsondata;

@jsondata:NumberConstraints {
    minimum: 4.0
}
public type SchemaItem0 int;

public type Schema json[0]|[SchemaItem0, string...];
