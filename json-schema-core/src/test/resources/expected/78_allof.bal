import ballerina/data.jsondata;

@jsondata:MetaData {
    title: "Person"
}
public type SchemaMainType json;

public type SchemaAllOf1 record {|
    string name;
    json...;
|};

public type SchemaAllOf2 record {|
    Age age;
    json...;
|};

@jsondata:NumberConstraints {
    minimum: 0.0
}
public type Age int;

@jsondata:AllOf
public type SchemaSubTypes SchemaAllOf1|SchemaAllOf2;

@jsondata:AllOf
public type Schema SchemaMainType|SchemaSubTypes;