import ballerina/data.jsondata;

@jsondata:StringValidation {
    minLength: 5
}
public type SchemaRestItemString string;

@jsondata:NumberValidation {
    minimum: 20.0
}
public type SchemaRestItemNumber int|float|decimal;

public type Schema [(SchemaRestItemString|SchemaRestItemNumber)...];
