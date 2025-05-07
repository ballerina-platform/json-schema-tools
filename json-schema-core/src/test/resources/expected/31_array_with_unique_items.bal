import ballerina/data.jsondata;

@jsondata:ArrayValidation {
    uniqueItems: true
}
public type Schema [string...];
