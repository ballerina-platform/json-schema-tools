import ballerina/data.jsondata;

@jsondata:ArrayConstraints {
    items: json
}
public type Schema [json...];
