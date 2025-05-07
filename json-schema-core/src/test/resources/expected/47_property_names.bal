import ballerina/data.jsondata;

@jsondata:ObjectValidation {
    propertyNames: SchemaPropertyNames
}
public type Schema record {|
    string name?;
    boolean...;
|};

@jsondata:StringValidation {
    pattern: re `^[a-z]+$`
}
public type SchemaPropertyNames string;
