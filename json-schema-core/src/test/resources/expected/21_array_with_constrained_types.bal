import ballerina/data.jsondata;

@jsondata:StringConstraints {
    minLength: 5
}
public type SchemaRestItemString string;

@jsondata:NumberConstraints {
    minimum: 20.0
}
public type SchemaRestItemNumber int|float|decimal;

public type Schema [(SchemaRestItemString|SchemaRestItemNumber)...];
