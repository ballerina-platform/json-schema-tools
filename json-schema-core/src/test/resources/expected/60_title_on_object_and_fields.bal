import ballerina/data.jsondata;

@jsondata:MetaData {
    title: "Person"
}
public type Schema record {|
    name name;
    Age age;
    json...;
|};

@jsondata:MetaData {
    title: "Full Name"
}
public type name string;

@jsondata:MetaData {
    title: "Age in years"
}
@jsondata:NumberConstraints {
    minimum: 0.0
}
public type Age int;
