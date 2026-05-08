import ballerina/data.jsondata;

@jsondata:ArrayConstraints {
    prefixItems: [SchemaItem0, SchemaItem1]
}
public type Schema [(SchemaItem0|SchemaItem1|SchemaRestItem)...];

# The first item must be a string representing a username.
public type SchemaItem0 string;

# The second item must be a non-negative integer representing user age.
@jsondata:NumberConstraints {
    minimum: 0.0
}
public type SchemaItem1 int;

# Any additional items must be booleans representing feature flags.
public type SchemaRestItem boolean;
