import ballerina/data.jsondata;

# A non-negative integer representing the number of items.
@jsondata:NumberConstraints {
    minimum: 0.0
}
public type Schema int;
