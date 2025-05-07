import ballerina/data.jsondata;

public type Schema record {|
    string occupation?;
    @jsondata:DependentRequired {
        value: ["age"]
    }
    string name;
    @jsondata:DependentRequired {
        value: ["name"]
    }
    int id;
    @jsondata:DependentRequired {
        value: ["id"]
    }
    int|float|decimal age;
    int...;
|};
