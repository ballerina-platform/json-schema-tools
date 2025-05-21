import ballerina/data.jsondata;

public type Schema record {|
    Name name;
    Age age;
    json...;
|};

# The full name of the person.
@jsondata:StringConstraints {
    minLength: 1
}
public type Name string;

# Age must be a non-negative integer.
@jsondata:NumberConstraints {
    minimum: 0.0
}
public type Age int;
