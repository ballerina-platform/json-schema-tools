import ballerina/data.jsondata;

@jsondata:ArrayConstraints {
    prefixItems: [int],
    maxItems: 4
}
public type Schema [(int|string)...];
