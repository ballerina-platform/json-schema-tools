import ballerina/data.jsondata;

public type Schema record {|
    # A system-generated unique identifier.
    id id?;
    # The user's login name.
    string username;
    json...;
|};

@jsondata:WriteOnly
public type id string;
