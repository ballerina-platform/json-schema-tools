import ballerina/data.jsondata;

@jsondata:NumberConstraints {
    multipleOf: 2.0
}
public type SchemaNotNumber int|float|decimal;

public type SchemaNot SchemaNotNumber|boolean|string|[json...]|record {|
    json...;
|}|();

@jsondata:Not {
    value: SchemaNot
}
public type Schema int;
