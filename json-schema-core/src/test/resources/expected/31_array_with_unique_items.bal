import ballerina/data.jsondata;

@jsondata:ArrayConstraints {
    uniqueItems: true
}
public type Schema [string...];
