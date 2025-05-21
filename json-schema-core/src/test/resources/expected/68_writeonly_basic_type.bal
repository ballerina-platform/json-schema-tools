import ballerina/data.jsondata;

# A system-generated numeric identifier that cannot be modified by the client.
@jsondata:MetaData {
    examples: [1001, 2025]
}
@jsondata:WriteOnly
@jsondata:NumberConstraints {
    minimum: 0.0
}
public type Schema int;
