import ballerina/data.jsondata;

@jsondata:ArrayConstraints {
    prefixItems: [int],
    maxItems: 100
}
public type Schema [(int|string)...];
