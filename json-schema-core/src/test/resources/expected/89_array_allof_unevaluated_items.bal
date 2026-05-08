import ballerina/data.jsondata;

@jsondata:ArrayConstraints {
    minItems: 1
}
public type SchemaAllOf2 [json, json...];

@jsondata:AllOf
public type SchemaSubTypes [int...]|SchemaAllOf2;

public type SchemaUnevaluatedItems int|float|decimal;

@jsondata:UnevaluatedItems {
	value: SchemaUnevaluatedItems
}
@jsondata:AllOf
public type Schema [json...]|SchemaSubTypes;
