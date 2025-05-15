import ballerina/data.jsondata;

@jsondata:MetaData {
    examples: [MAPPING_1, MAPPING_2]
}
public type Schema record {|
    # This is a dummy description
    dummy dummy = true;
    Age age;
    username username;
    json...;
|};

@jsondata:MetaData {
    examples: ["alice123", "bob_smith"]
}
public type username string;

@jsondata:MetaData {
    comment: "User age in years; must be non-negative."
}
@jsondata:NumberConstraints {
    minimum: 0.0
}
public type Age int;

@jsondata:MetaData {
    title: "This is a dummy title",
    comment: "This is a dummy comment."
}
public type dummy boolean;

public const MAPPING_1 = {"username": "alice123", "age": 30};
public const MAPPING_2 = {"username": "bob_smith", "age": 45};
