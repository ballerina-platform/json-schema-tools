import ballerina/data.jsondata;

@jsondata:MetaData {
    title: "Image Upload"
}
public type Schema record {|
    # A PNG image encoded in base64.
    ImageData imageData;
    json...;
|};

@jsondata:StringEncodedData {
    contentEncoding: "base64",
    contentMediaType: "image/png"
}
public type ImageData string;
