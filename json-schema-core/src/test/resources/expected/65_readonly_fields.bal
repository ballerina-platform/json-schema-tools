public type Schema record {|
    # A system-generated unique identifier.
    readonly string id?;
    # The user's login name.
    string username;
    json...;
|};
