import ballerina/data.jsondata;

public type Schema record {|
    string occupation?;
    json name?;
    int id;
    @jsondata:DependentSchema {
        value: AgeDependentSchema1
    }
    int|float|decimal age?;
    int...;
|};

public type AgeDependentSchema1 record {|
    string name?;
    json...;
|};
