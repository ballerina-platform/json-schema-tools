import ballerina/data.jsondata;

@jsondata:AdditionalProperties {
    value: json
}
public type Schema record {|
    json...;
|};
