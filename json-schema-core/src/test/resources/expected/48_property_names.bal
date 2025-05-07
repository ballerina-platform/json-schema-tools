import ballerina/data.jsondata;

@jsondata:ObjectValidation {
    propertyNames: string
}
public type Schema record {|
    string name?;
    boolean...;
|};
