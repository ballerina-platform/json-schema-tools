import ballerina/data.jsondata;

@jsondata:ArrayConstraints {
    minItems: 10
}
public type Schema [int, string...];
