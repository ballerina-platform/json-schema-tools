import ballerina/data.jsondata;

public type SchemaOneOf1 json;

public type SchemaOneOf2 json;

public type SchemaOneOf3 json;

@jsondata:OneOf
public type SchemaSubTypes SchemaOneOf1|SchemaOneOf2|SchemaOneOf3;

@jsondata:AllOf
public type Schema json|SchemaSubTypes;
