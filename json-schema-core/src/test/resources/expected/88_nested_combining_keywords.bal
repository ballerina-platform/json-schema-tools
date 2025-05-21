import ballerina/data.jsondata;

@jsondata:Not {
    value: SchemaNot
}
public type Schema record {|
    int code?;
    string status?;
    json...;
|};

public type SchemaNotAllOf1AllOf1Object record {|
    json status;
    json...;
|};

public type SchemaNotAllOf1AllOf1 boolean|string|[json...]|SchemaNotAllOf1AllOf1Object|()|int|float|decimal;

public type SchemaNotAllOf1AllOf2Object record {|
    "error" status?;
    json...;
|};

public type SchemaNotAllOf1AllOf2 boolean|string|[json...]|SchemaNotAllOf1AllOf2Object|()|int|float|decimal;

@jsondata:AllOf
public type SchemaNotAllOf1SubTypes SchemaNotAllOf1AllOf1|SchemaNotAllOf1AllOf2;

@jsondata:AllOf
public type SchemaNotAllOf1 json|SchemaNotAllOf1SubTypes;

public type SchemaNotAllOf2Object record {|
    CodeNumber|boolean|string|[json...]|record {|
        json...;
    |}|() code?;
    json...;
|};

@jsondata:NumberConstraints {
    minimum: 500.0
}
public type CodeNumber int|float|decimal;

public type SchemaNotAllOf2 boolean|string|[json...]|SchemaNotAllOf2Object|()|int|float|decimal;

@jsondata:AllOf
public type SchemaNotSubTypes SchemaNotAllOf1|SchemaNotAllOf2;

@jsondata:AllOf
public type SchemaNot json|SchemaNotSubTypes;
