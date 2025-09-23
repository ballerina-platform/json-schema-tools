import ballerina/data.jsondata;

public type Schema record {|
    string occupation?;
    json name?;
    int id;
    @jsondata:DependentSchema {
        value: AgeDependentSchema
    }
    int|float|decimal age?;
    int...;
|};

public type AgeDependentSchema record {|
    string name?;
    json...;
|};
