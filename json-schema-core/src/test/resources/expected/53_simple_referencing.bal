public type Schema record {|
    Employee employee?;
    json...;
|};

public type Employee record {|
    string name;
    int age?;
    json...;
|};
