import ballerina/data.jsondata;

@jsondata:NumberValidation {
    minimum: 20.0
}
public type SchemaRestItemNumber int|float|decimal;

@jsondata:StringValidation {
    minLength: 5
}
public type SchemaRestItemString string;

public type Schema [(SchemaRestItemString|SchemaRestItemNumber)...];
