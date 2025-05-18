import ballerina/data.jsondata;

public type Schema record {|
    json[0]|[string]|[string, DataItem1, DataItem1...] data?;
    json...;
|};

@jsondata:NumberConstraints {
    minimum: 10.0
}
public type DataItem1 int|float|decimal;
