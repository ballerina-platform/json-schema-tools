import ballerina/data.jsondata;

@jsondata:NumberConstraints {
    minimum: 20.0,
    multipleOf: 5.0
}
public type Schema int|float|decimal;
