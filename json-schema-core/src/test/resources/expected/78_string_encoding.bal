import ballerina/data.jsondata;

@jsondata:MetaData {
    title: "Encoded User Info"
}
public type Schema record {|
    # Base64-encoded JSON object containing user information.
    UserInfo userInfo;
    json...;
|};

public type UserInfoContentSchema record {|
    int id;
    Email email;
    json...;
|};

@jsondata:StringConstraints {
    format: "email"
}
public type Email string;

@jsondata:StringEncodedData {
    contentEncoding: "base64",
    contentMediaType: "application/json",
    contentSchema: UserInfoContentSchema
}
public type UserInfo string;
