import ballerina/data.jsondata;

@jsondata:ArrayConstraints {
    prefixItems: [int, boolean]
}
public type Schema [(int|boolean|string)...];
