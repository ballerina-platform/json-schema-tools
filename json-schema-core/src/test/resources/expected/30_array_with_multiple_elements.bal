import ballerina/data.jsondata;

@jsondata:ArrayValidation {
    maxItems: 100
}
public type Schema json[0]|[int, string...];
