import ballerina/data.jsondata;

@jsondata:NumberConstraints {
    minimum: 20.0,
    maximum: 40.0
}
public type Schema int;
