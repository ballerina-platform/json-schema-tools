public type Schema record {|
    # An old scoring system no longer in use.
    @deprecated
    int legacyScore?;
    # A unique identifier for the user.
    int userId;
    json...;
|};
