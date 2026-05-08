import ballerina/data.jsondata;

public type SchemaMainType record {|
    json...;
|};

public type SchemaAllOf1 record {|
    string name;
    json...;
|};

public type SchemaAllOf2MainType record {|
    json...;
|};

public type SchemaAllOf2AnyOf1 record {|
    Age age;
    json...;
|};

@jsondata:NumberConstraints {
    minimum: 18.0
}
public type Age int|float|decimal;

public type SchemaAllOf2AnyOf2 record {|
    true guardianConsent;
    json...;
|};

public type SchemaAllOf2SubTypes SchemaAllOf2AnyOf1|SchemaAllOf2AnyOf2;

@jsondata:AllOf
public type SchemaAllOf2 SchemaAllOf2MainType|SchemaAllOf2SubTypes;

public type SchemaAllOf3MainType record {|
    json...;
|};

public type SchemaAllOf3OneOf1 record {|
    "admin" role;
    string adminCode;
    json...;
|};

public type SchemaAllOf3OneOf2 record {|
    "user" role;
    json...;
|};

@jsondata:OneOf
public type SchemaAllOf3SubTypes SchemaAllOf3OneOf1|SchemaAllOf3OneOf2;

@jsondata:AllOf
public type SchemaAllOf3 SchemaAllOf3MainType|SchemaAllOf3SubTypes;

@jsondata:AllOf
public type SchemaSubTypes SchemaAllOf1|SchemaAllOf2|SchemaAllOf3;

@jsondata:AllOf
public type Schema SchemaMainType|SchemaSubTypes;
