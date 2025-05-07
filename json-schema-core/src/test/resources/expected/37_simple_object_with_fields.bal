public type Schema record {|
    "doctor"|"teacher"|"engineer" occupation?;
    string name?;
    int|float|decimal age?;
    json...;
|};
