public type Schema record {|
    string name;
    string|int id;
    int|float|decimal age?;
    (string|int)...;
|};
