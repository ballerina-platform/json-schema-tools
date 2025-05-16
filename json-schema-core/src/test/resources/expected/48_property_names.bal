import ballerina/data.jsondata;

@jsondata:ObjectConstraints {
    propertyNames: string
}
public type Schema record {|
    string name?;
    boolean...;
|};
