import ballerina/data.jsondata;

@jsondata:ArrayConstraints {
    prefixItems: [json, SchemaItem1],
    minItems: 1
}
public type Schema [json, (SchemaItem1|SchemaRestItem)...];

public type SchemaItem1 json;

public type SchemaRestItem json;
