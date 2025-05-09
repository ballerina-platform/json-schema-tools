import ballerina/data.jsondata;

@jsondata:ObjectConstraints {
    propertyNames: SchemaPropertyNames
}
public type Schema record {|
    string name?;
    boolean...;
|};

@jsondata:StringConstraints {
    pattern: re `^[a-z]+$`
}
public type SchemaPropertyNames string;
