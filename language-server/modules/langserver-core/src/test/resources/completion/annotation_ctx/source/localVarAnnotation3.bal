import ballerina/module1;

public type AnnotationType record {
    string foo;
    int bar?;
};

public const annotation AnnotationType sourceVarAnnotation1 on source var;

public const annotation sourceVarAnnotation2 on source var;

function testFunction() {
    @module1:
    int testModuleVar = 12;
}
