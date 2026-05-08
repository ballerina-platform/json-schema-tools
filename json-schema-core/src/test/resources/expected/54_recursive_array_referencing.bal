import ballerina/data.jsondata;

@jsondata:ArrayConstraints {
    prefixItems: [string]
}
public type Schema [(string|Schema)...];
