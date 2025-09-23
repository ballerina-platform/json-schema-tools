public type Schema record {|
    boolean subscribed?;
    string name = "Anonymous";
    int|float|decimal age?;
    json...;
|};
