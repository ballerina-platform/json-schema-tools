import ballerina/data.jsondata;

public type Schema record {|
    Metadata metadata?;
    # The type of entity in the organization.
    "Department"|"Team"|"Person" entity_type;
    # List of members or sub-units under this entity.
    [Schema...] members?;
    # The name of the entity.
    string name;
    json...;
|};

public type Metadata record {|
    Created created?;
    boolean active?;
    json...;
|};

@jsondata:StringConstraints {
    format: "date"
}
public type Created string;
