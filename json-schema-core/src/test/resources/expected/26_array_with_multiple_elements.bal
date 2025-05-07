import ballerina/data.jsondata;

@jsondata:ArrayValidation {
    minItems: 10
}
public type Schema [int, string...];
