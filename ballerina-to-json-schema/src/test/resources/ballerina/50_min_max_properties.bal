import ballerina/data.jsondata;

@jsondata:ObjectConstraints {
    minProperties: 2,
    maxProperties: 4
}
public type Schema record {|
    string country?;
    string name?;
    int|float|decimal age?;
    string email?;
    json...;
|};
