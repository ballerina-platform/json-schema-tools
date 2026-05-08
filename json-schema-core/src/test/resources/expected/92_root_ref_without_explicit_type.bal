public type SchemaObject record {|
    Schema foo?;
|};

public type Schema boolean|string|[json...]|SchemaObject|()|int|float|decimal;
