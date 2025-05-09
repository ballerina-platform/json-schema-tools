import ballerina/data.jsondata;

@jsondata:AdditionalProperties {
    value: json
}
@jsondata:PatternProperties {
    value: [schemaPatternElement1, schemaPatternElement2]
}
public type Schema record {|
    boolean foo1?;
    string foo2?;
    json foo3;
    json...;
|};

jsondata:PatternPropertiesElement schemaPatternElement1 = {
    pattern: re `^str_\w+$`,
    value: string
};

public type schemaPatternElement2Type int|float|decimal;

jsondata:PatternPropertiesElement schemaPatternElement2 = {
    pattern: re `^num_\w+$`,
    value: schemaPatternElement2Type
};
