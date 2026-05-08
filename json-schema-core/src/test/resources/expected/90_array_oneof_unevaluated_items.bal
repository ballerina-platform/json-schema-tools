import ballerina/data.jsondata;

@jsondata:ArrayConstraints {
    prefixItems: [string]
}
public type SchemaOneOf2 [(string|json)...];

@jsondata:OneOf
public type SchemaSubTypes [int...]|SchemaOneOf2;

@jsondata:UnevaluatedItems {
	value: string
}
@jsondata:AllOf
public type Schema [json...]|SchemaSubTypes;
