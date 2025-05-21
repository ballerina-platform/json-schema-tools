import ballerina/data.jsondata;

# The first item must be a string representing a username.
public type SchemaItem0 string;

# The second item must be a non-negative integer representing user age.
@jsondata:NumberConstraints {
    minimum: 0.0
}
public type SchemaItem1 int;

# Any additional items must be booleans representing feature flags.
public type SchemaRestItem boolean;

public type Schema json[0]|[SchemaItem0]|[SchemaItem0, SchemaItem1, SchemaRestItem...];
