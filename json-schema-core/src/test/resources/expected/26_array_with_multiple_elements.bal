import ballerina/data.jsondata;

@jsondata:ArrayConstraints {
    prefixItems: [int],
    minItems: 10
}
public type Schema [int, string, string, string, string, string, string, string, string, string, string...];
