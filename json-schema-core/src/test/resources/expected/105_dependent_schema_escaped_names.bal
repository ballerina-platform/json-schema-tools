import ballerina/data.jsondata;

@jsondata:ObjectConstraints {
    dependentSchemas: [{property: "foo\tbar", schema: Foo_barDependentSchema}, {property: "foo'bar", schema: Foo_barDependentSchema1}]
}
public type SchemaObject record {|
    json...;
|};

@jsondata:ObjectConstraints {
    minProperties: 4
}
public type Foo_barDependentSchemaObject record {|
    json...;
|};

public type Foo_barDependentSchema boolean|string|[json...]|Foo_barDependentSchemaObject|()|int|float|decimal;

public type Foo_barDependentSchema1Object record {|
    json 'foo\u{22}bar;
    json...;
|};

public type Foo_barDependentSchema1 boolean|string|[json...]|Foo_barDependentSchema1Object|()|int|float|decimal;

public type Schema boolean|string|[json...]|SchemaObject|()|int|float|decimal;
