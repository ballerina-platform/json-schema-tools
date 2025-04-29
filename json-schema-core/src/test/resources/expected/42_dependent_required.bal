import ballerina/data.jsondata;

public type Schema record {|
    string occupation?;
    string name;
    @jsondata:DependentRequired {
        value: ["name"]
    }
    int id;
    @jsondata:DependentRequired {
        value: ["name"]
    }
    int|float|decimal age?;
    int...;
|};
