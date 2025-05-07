import ballerina/data.jsondata;

public type Schema record {|
    "doctor"|"teacher"|"engineer" occupation?;
    string name;
    Id id;
    int|float|decimal age?;
    json...;
|};

@jsondata:NumberValidation {
    minimum: 0.0
}
public type Id int;
