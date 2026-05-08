public type Schema record {|
    # Child nodes of this node
    [Schema...] children?;
    # The name of the node
    string name;
    json...;
|};
