import ballerina/data.jsondata;

@jsondata:StringConstraints {
    format: "email",
    minLength: 1,
    maxLength: 10,
    pattern: re `^[a-zA-Z0-9_.+-]+$`
}
public type Schema string;
