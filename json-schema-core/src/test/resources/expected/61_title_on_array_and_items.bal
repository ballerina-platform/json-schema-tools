import ballerina/data.jsondata;

@jsondata:MetaData {
    title: "User Roles List"
}
@jsondata:ArrayConstraints {
    minItems: 2
}
public type Schema [SchemaItem0, SchemaItem1, SchemaRestItem...];

@jsondata:MetaData {
    title: "Primary Role"
}
public type SchemaItem0 string;

@jsondata:MetaData {
    title: "Secondary Role"
}
public type SchemaItem1 string;

@jsondata:MetaData {
    title: "Additional Role"
}
public type SchemaRestItem string;
