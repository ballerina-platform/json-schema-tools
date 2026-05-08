import ballerina/data.jsondata;

@jsondata:ArrayConstraints {
    prefixItems: [int],
    minItems: 3
}
public type Schema [int, string, string, string...];
