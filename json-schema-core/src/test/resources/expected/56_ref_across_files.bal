import ballerina/data.jsondata;

public type Schema record {|
    Name name;
    json...;
|};

@jsondata:StringConstraints {
    minLength: 1
}
public type Name string;
