import ballerina/data.jsondata;

public type Schema record {|
    Preferences preferences?;
    User user;
    json...;
|};

public type User record {|
    Address address?;
    Name name;
    # System-generated user ID
    readonly int id;
    Email email;
    json...;
|};

# Full name of the user
@jsondata:StringConstraints {
    minLength: 1
}
public type Name string;

@jsondata:StringConstraints {
    format: "email"
}
public type Email string;

public type Address record {|
    string city;
    string street;
    string postalCode;
    json...;
|};

public type Preferences record {|
    "light"|"dark" theme?;
    Notifications notifications?;
    json...;
|};

public type Notifications record {|
    boolean sms?;
    boolean email = true;
    json...;
|};
