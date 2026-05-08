import ballerina/data.jsondata;

@jsondata:UnevaluatedProperties {
    value: int
}
public type Schema record {|
    string occupation?;
    json name?;
    int id;
    @jsondata:DependentSchema {
        value: AgeDependentSchema
    }
    int|float|decimal age?;
    json...;
|};

public type AgeDependentSchema record {|
    string name?;
    json...;
|};
