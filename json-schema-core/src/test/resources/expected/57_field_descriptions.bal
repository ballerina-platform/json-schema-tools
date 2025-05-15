import ballerina/data.jsondata;

# A schema representing a basic user object with username, age, and email.
public type Schema record {|
    # The age of the user. Must be a non-negative integer.
    Age age?;
    # The user's email address in valid format.
    Email email;
    # The unique username of the user.
    string username;
    json...;
|};

@jsondata:NumberConstraints {
    minimum: 0.0
}
public type Age int;

@jsondata:StringConstraints {
    format: "email"
}
public type Email string;
