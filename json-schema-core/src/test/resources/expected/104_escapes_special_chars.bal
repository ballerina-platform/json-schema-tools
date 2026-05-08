import ballerina/data.jsondata;

public type Schema record {|
    json 'foo\u{D}bar?;
    @jsondata:DependentRequired {
        value: ["foo'bar"]
    }
    json 'foo\u{22}bar?;
    json 'foo\u{27}bar?;
    @jsondata:DependentRequired {
        value: ["foo\rbar"]
    }
    json 'foo\u{A}bar?;
    json...;
|};
