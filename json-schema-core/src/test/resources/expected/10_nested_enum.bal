public const enumObject1 = {"key1": 100, "key2": true};
public const enumObject2 = {"key1": 100, "key2": enumObject3, "key3": ["item1", 10]};
public const enumObject3 = {"nestedKey": 200};

public type Schema ()|3.14|enumObject1|"exampleString"|42|enumObject2|true|[1, 2, "sample"];
