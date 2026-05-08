public type SchemaObject record {|
    int slash?;
    int tilde?;
    int percent?;
    json...;
|};

public type Schema boolean|string|[json...]|SchemaObject|()|int|float|decimal;
