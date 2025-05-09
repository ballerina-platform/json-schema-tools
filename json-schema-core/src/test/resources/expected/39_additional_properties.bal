public type Schema record {|
    string name;
    int|string id;
    int|float|decimal age?;
    (int|string)...;
|};
