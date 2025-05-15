public const MAPPING_2 = {"nestedKey": 200};
public const MAPPING_1 = {"key1": 100, "key2": MAPPING_2, "key3": ["item1", 10]};
public const MAPPING_3 = {"key1": 100, "key2": true};

public type Schema 42|()|3.14|"exampleString"|true|[1, 2, "sample"]|MAPPING_1|MAPPING_3;
