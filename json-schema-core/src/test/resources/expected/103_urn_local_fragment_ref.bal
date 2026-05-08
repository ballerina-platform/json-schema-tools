public type SchemaObject record {|
    int value?;
    json...;
|};

public type Schema boolean|string|[json...]|SchemaObject|()|int|float|decimal;
