import ballerina/data.jsondata;

@jsondata:PatternProperties {
    value: [schemaPatternElement1]
}
public type Schema record {|
    json...;
|};

public type schemaPatternElement1Type "foo"|"bar";

jsondata:PatternPropertiesElement schemaPatternElement1 = {
    pattern: re `^foo$`,
    value: schemaPatternElement1Type
};
