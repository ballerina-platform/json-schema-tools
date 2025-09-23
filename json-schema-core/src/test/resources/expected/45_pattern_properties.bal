import ballerina/data.jsondata;

@jsondata:AdditionalProperties {
    value: boolean
}
@jsondata:PatternProperties {
    value: [schemaPatternElement1, schemaPatternElement2]
}
public type Schema record {|
    (boolean|string|schemaPatternElement2Type)...;
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
