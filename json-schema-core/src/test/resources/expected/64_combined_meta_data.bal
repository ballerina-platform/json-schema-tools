import ballerina/data.jsondata;

# The age of the user in whole years.
@jsondata:MetaData {
    title: "User Age",
    comment: "This must be a non-negative integer and should represent a realistic human age.",
    examples: [0, 25, 65]
}
@jsondata:NumberConstraints {
    minimum: 0.0,
    maximum: 130.0
}
public type Schema int;
