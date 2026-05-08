import ballerina/data.jsondata;

public type Schema record {|
    Data data?;
    json...;
|};

@jsondata:ArrayConstraints {
    prefixItems: [string, DataItem11]
}
public type Data [(string|DataItem11|DataRestItem)...];

@jsondata:NumberConstraints {
    minimum: 10.0
}
public type DataItem1 int|float|decimal;

public type DataItem11 DataItem1;

public type DataRestItem DataItem1;
