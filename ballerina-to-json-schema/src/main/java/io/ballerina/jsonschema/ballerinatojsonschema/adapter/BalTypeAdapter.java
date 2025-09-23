/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com)
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.jsonschema.ballerinatojsonschema.adapter;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.ballerina.compiler.api.symbols.ArrayTypeSymbol;
import io.ballerina.compiler.api.symbols.MapTypeSymbol;
import io.ballerina.compiler.api.symbols.RecordFieldSymbol;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingFieldNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.ballerina.jsonschema.ballerinatojsonschema.BallerinaBasicType;
import io.ballerina.jsonschema.ballerinatojsonschema.BasicTypeMapper;
import io.ballerina.jsonschema.ballerinatojsonschema.JsonSchemaConstants;
import io.ballerina.jsonschema.ballerinatojsonschema.JsonSchemaNode;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class BalTypeAdapter {
    private BalTypeAdapter() {}

    public static JsonSchemaNode toSchemaNode(
            TypeSymbol ballerinaTypeSymbol,
            boolean allowOptionalNull,
            Optional<MetadataNode> typedefMetadataForThisType,
            Map<String, MetadataNode> recordFieldMetadataByName,
            Map<String, MetadataNode> typedefMetadataByName) {

        ballerinaTypeSymbol = dereferenceTypeReferences(ballerinaTypeSymbol);

        // Nullable shorthand: union(T, nil) -> rewrite underlying schema's "type" as ["<type>","null"]
        if (allowOptionalNull && ballerinaTypeSymbol.typeKind() == TypeDescKind.UNION) {
            UnionTypeSymbol unionTypeSymbol = (UnionTypeSymbol) ballerinaTypeSymbol;
            if (unionTypeSymbol.memberTypeDescriptors().size() == 2) {
                TypeSymbol firstMember = dereferenceTypeReferences(unionTypeSymbol.memberTypeDescriptors().get(0));
                TypeSymbol secondMember = dereferenceTypeReferences(unionTypeSymbol.memberTypeDescriptors().get(1));
                TypeSymbol nonNilMember = isNilType(firstMember) ? secondMember
                        : (isNilType(secondMember) ? firstMember : null);
                if (nonNilMember != null) {
                    JsonSchemaNode nonNilSchemaNode = toSchemaNode(
                            nonNilMember, /*allowOptionalNull*/ false,
                            typedefMetadataForThisType, recordFieldMetadataByName, typedefMetadataByName);
                    makeTypeNullableInPlace(nonNilSchemaNode);
                    return nonNilSchemaNode;
                }
            }
        }

        JsonSchemaNode baseSchemaNode = switch (ballerinaTypeSymbol.typeKind()) {
            case INT, FLOAT, DECIMAL, STRING, BOOLEAN, NIL -> mapBasicTypeToSchema(ballerinaTypeSymbol);
            case ARRAY -> buildArraySchema(
                    (ArrayTypeSymbol) ballerinaTypeSymbol, allowOptionalNull,
                    typedefMetadataByName);
            case RECORD -> buildRecordSchema(
                    (RecordTypeSymbol) ballerinaTypeSymbol, allowOptionalNull,
                    recordFieldMetadataByName, typedefMetadataByName);
            case MAP -> buildMapSchema(
                    (MapTypeSymbol) ballerinaTypeSymbol, allowOptionalNull,
                    typedefMetadataByName);
            case UNION -> buildUnionSchemaAnyOf(
                    (UnionTypeSymbol) ballerinaTypeSymbol, allowOptionalNull,
                    typedefMetadataForThisType, recordFieldMetadataByName, typedefMetadataByName);
            default -> throw new IllegalArgumentException("Unsupported Ballerina type: "
                    + ballerinaTypeSymbol.typeKind());
        };

        // Apply typedef-level constraints (if this call corresponds to a root typedef)
        if (typedefMetadataForThisType != null && typedefMetadataForThisType.isPresent()) {
            applyConstraintsFromBallerinaConstraintAnnotations(
                    baseSchemaNode.toObjectNode(), typedefMetadataForThisType.get(),
                    dereferenceTypeReferences(ballerinaTypeSymbol).typeKind());
        }

        return baseSchemaNode;
    }

    /** Convenience overloads for older call sites. */
    public static JsonSchemaNode toSchemaNode(TypeSymbol typeSymbol, boolean allowOptionalNull) {
        return toSchemaNode(typeSymbol, allowOptionalNull, Optional.empty(), Map.of(), Map.of());
    }

    public static JsonSchemaNode toBasicTypeSchemaNode(TypeSymbol typeSymbol, boolean allowOptionalAsTypeArray) {
        JsonSchemaNode schemaNode = toSchemaNode(typeSymbol, allowOptionalAsTypeArray, Optional.empty(), Map.of(), Map.of());
        return switch (dereferenceTypeReferences(typeSymbol).typeKind()) {
            case INT, FLOAT, DECIMAL, STRING, BOOLEAN, NIL, UNION -> schemaNode;
            default -> throw new IllegalArgumentException("Unsupported type for Stage 1: " + typeSymbol.typeKind());
        };
    }

    private static JsonSchemaNode buildUnionSchemaAnyOf(
            UnionTypeSymbol unionTypeSymbol,
            boolean allowOptionalNull,
            Optional<MetadataNode> typedefMetadataForThisType,
            Map<String, MetadataNode> recordFieldMetadataByName,
            Map<String, MetadataNode> typedefMetadataByName) {

        ObjectNode unionSchemaObject = JsonSchemaNode.mapper().createObjectNode();
        ArrayNode anyOfArrayNode = JsonSchemaNode.mapper().createArrayNode();

        // De-duplicate member schemas for stability
        Set<String> uniqueMemberSerializations = new LinkedHashSet<>();

        for (TypeSymbol memberTypeSymbol : unionTypeSymbol.memberTypeDescriptors()) {
            JsonSchemaNode memberSchemaNode = toSchemaNode(
                    dereferenceTypeReferences(memberTypeSymbol), false,
                    Optional.empty(), recordFieldMetadataByName, typedefMetadataByName);

            ObjectNode memberObjectNode = memberSchemaNode.toObjectNode();
            String serialized = memberObjectNode.toString();
            if (uniqueMemberSerializations.add(serialized)) {
                anyOfArrayNode.add(memberObjectNode);
            }
        }

        unionSchemaObject.set(JsonSchemaConstants.K_ANY_OF, anyOfArrayNode);
        return wrap(unionSchemaObject);
    }

    private static JsonSchemaNode buildArraySchema(
            ArrayTypeSymbol arrayTypeSymbol,
            boolean allowOptionalNull,
            Map<String, MetadataNode> typedefMetadataByName) {

        TypeSymbol memberTypeSymbol = arrayTypeSymbol.memberTypeDescriptor();
        JsonSchemaNode itemsSchemaNode = toSchemaNode(
                memberTypeSymbol, allowOptionalNull,
                Optional.empty(), Map.of(), typedefMetadataByName);

        ObjectNode arraySchemaObject = JsonSchemaNode.mapper().createObjectNode();
        arraySchemaObject.put(JsonSchemaConstants.K_TYPE, JsonSchemaConstants.T_ARRAY);
        arraySchemaObject.set("items", itemsSchemaNode.toObjectNode());
        return wrap(arraySchemaObject);
    }

    private static JsonSchemaNode buildRecordSchema(
            RecordTypeSymbol recordTypeSymbol,
            boolean allowOptionalNull,
            Map<String, MetadataNode> recordFieldMetadataByName,
            Map<String, MetadataNode> typedefMetadataByName) {

        ObjectNode recordSchemaObject = JsonSchemaNode.mapper().createObjectNode();
        recordSchemaObject.put(JsonSchemaConstants.K_TYPE, JsonSchemaConstants.T_OBJECT);

        ObjectNode propertiesObject = JsonSchemaNode.mapper().createObjectNode();
        ArrayNode requiredFieldsArray = JsonSchemaNode.mapper().createArrayNode();
        boolean hasAnyRequiredField = false;

        for (Map.Entry<String, RecordFieldSymbol> fieldEntry : recordTypeSymbol.fieldDescriptors().entrySet()) {
            String fieldName = fieldEntry.getKey();
            RecordFieldSymbol recordFieldSymbol = fieldEntry.getValue();

            TypeSymbol originalFieldTypeSymbol = recordFieldSymbol.typeDescriptor();
            TypeSymbol dereferencedFieldTypeSymbol = dereferenceTypeReferences(originalFieldTypeSymbol);

            JsonSchemaNode fieldSchemaNode = toSchemaNode(
                    dereferencedFieldTypeSymbol, allowOptionalNull,
                    Optional.empty(), Map.of(), typedefMetadataByName);

            ObjectNode fieldSchemaObject = fieldSchemaNode.toObjectNode();

            Optional<String> referencedTypeName = optionalTypeName(originalFieldTypeSymbol);
            if (referencedTypeName.isPresent()) {
                MetadataNode aliasMetadata = typedefMetadataByName.get(referencedTypeName.get());
                if (aliasMetadata != null) {
                    applyConstraintsFromBallerinaConstraintAnnotations(
                            fieldSchemaObject, aliasMetadata, dereferencedFieldTypeSymbol.typeKind());
                }
            }

            MetadataNode fieldMetadata = recordFieldMetadataByName.get(fieldName);
            if (fieldMetadata != null) {
                applyConstraintsFromBallerinaConstraintAnnotations(
                        fieldSchemaObject, fieldMetadata, dereferencedFieldTypeSymbol.typeKind());
            }

            propertiesObject.set(fieldName, fieldSchemaObject);

            if (!recordFieldSymbol.isOptional()) {
                requiredFieldsArray.add(fieldName);
                hasAnyRequiredField = true;
            }
        }

        recordSchemaObject.set(JsonSchemaConstants.K_PROPERTIES, propertiesObject);
        if (hasAnyRequiredField) {
            recordSchemaObject.set(JsonSchemaConstants.K_REQUIRED, requiredFieldsArray);
        }

        Optional<TypeSymbol> restFieldTypeOptional = recordTypeSymbol.restTypeDescriptor();
        if (restFieldTypeOptional.isPresent()) {
            JsonSchemaNode additionalPropertiesSchemaNode =
                    toSchemaNode(restFieldTypeOptional.get(), allowOptionalNull,
                            Optional.empty(), Map.of(), typedefMetadataByName);
            recordSchemaObject.set(JsonSchemaConstants.K_ADDITIONAL_PROPERTIES,
                    additionalPropertiesSchemaNode.toObjectNode());
        } else {
            recordSchemaObject.put(JsonSchemaConstants.K_ADDITIONAL_PROPERTIES, false);
        }

        return wrap(recordSchemaObject);
    }

    private static JsonSchemaNode buildMapSchema(
            MapTypeSymbol mapTypeSymbol,
            boolean allowOptionalNull,
            Map<String, MetadataNode> typedefMetadataByName) {

        TypeSymbol mapValueTypeSymbol = resolveMapValueType(mapTypeSymbol);

        ObjectNode mapSchemaObject = JsonSchemaNode.mapper().createObjectNode();
        mapSchemaObject.put(JsonSchemaConstants.K_TYPE, JsonSchemaConstants.T_OBJECT);

        if (mapValueTypeSymbol != null) {
            JsonSchemaNode valueSchemaNode =
                    toSchemaNode(mapValueTypeSymbol, allowOptionalNull,
                            Optional.empty(), Map.of(), typedefMetadataByName);
            mapSchemaObject.set(JsonSchemaConstants.K_ADDITIONAL_PROPERTIES, valueSchemaNode.toObjectNode());
        } else {
            // map<anydata>: additionalProperties is an unconstrained schema {}
            mapSchemaObject.set(JsonSchemaConstants.K_ADDITIONAL_PROPERTIES,
                    JsonSchemaNode.mapper().createObjectNode());
        }
        return wrap(mapSchemaObject);
    }

    private static void applyConstraintsFromBallerinaConstraintAnnotations(
            ObjectNode targetSchemaObject,
            MetadataNode metadataNode,
            TypeDescKind baseKindAfterDereference) {

        for (AnnotationNode annotationNode : metadataNode.annotations()) {
            String qualifiedName = annotationNode.annotReference().toString().trim(); // e.g. "constraint:String"
            String modulePrefix;
            String annotationKind;
            if (annotationNode.annotReference() instanceof QualifiedNameReferenceNode qnr) {
                modulePrefix = qnr.modulePrefix().text().trim();
                annotationKind = qnr.identifier().text().trim();
            } else {
                continue;
            }
            if (!"constraint".equals(modulePrefix)) continue;

            Optional<MappingConstructorExpressionNode> maybeMapping = annotationNode.annotValue();
            if (maybeMapping.isEmpty()) continue;

            Map<String, String> keyValues = readSimpleKeyValueMapping(maybeMapping.get());

            switch (annotationKind) {
                case "Int":
                case "Float":
                case "Decimal":
                    if (baseKindAfterDereference == TypeDescKind.INT
                            || baseKindAfterDereference == TypeDescKind.FLOAT
                            || baseKindAfterDereference == TypeDescKind.DECIMAL) {
                        applyNumberConstraints(targetSchemaObject, keyValues);
                    }
                    break;

                case "String":
                    if (baseKindAfterDereference == TypeDescKind.STRING) {
                        applyStringConstraints(targetSchemaObject, keyValues);
                    }
                    break;

                case "Array":
                    if (baseKindAfterDereference == TypeDescKind.ARRAY) {
                        applyArrayConstraints(targetSchemaObject, keyValues);
                    }
                    break;

                default:
                    break;
            }
        }
    }

    private static Map<String, String> readSimpleKeyValueMapping(MappingConstructorExpressionNode mapping) {
        Map<String, String> result = new LinkedHashMap<>();
        for (MappingFieldNode fieldNode : mapping.fields()) {
            if (fieldNode instanceof SpecificFieldNode specificFieldNode
                    && specificFieldNode.valueExpr().isPresent()) {
                String key = specificFieldNode.fieldName().toString().trim();
                String valueSource = specificFieldNode.valueExpr().get().toString().trim();
                result.put(key, valueSource);
            }
        }
        return result;
    }

    private static void applyNumberConstraints(ObjectNode schemaObject, Map<String, String> keyValues) {
        // Ballerina: minValue/maxValue + exclusiveMin/exclusiveMax (booleans)
        // JSON Schema 2020-12: exclusiveMinimum/exclusiveMaximum are NUMBERS (not booleans)
        if (keyValues.containsKey("minValue")) {
            String minValueText = normalizeNumericLiteral(keyValues.get("minValue"));
            boolean exclusive = parseBooleanOrDefault(keyValues.get("exclusiveMin"), false);
            if (exclusive) {
                schemaObject.put(JsonSchemaConstants.K_EXCLUSIVE_MINIMUM, new BigDecimal(minValueText));
            } else {
                schemaObject.put(JsonSchemaConstants.K_MINIMUM, new BigDecimal(minValueText));
            }
        }
        if (keyValues.containsKey("maxValue")) {
            String maxValueText = normalizeNumericLiteral(keyValues.get("maxValue"));
            boolean exclusive = parseBooleanOrDefault(keyValues.get("exclusiveMax"), false);
            if (exclusive) {
                schemaObject.put(JsonSchemaConstants.K_EXCLUSIVE_MAXIMUM, new BigDecimal(maxValueText));
            } else {
                schemaObject.put(JsonSchemaConstants.K_MAXIMUM, new BigDecimal(maxValueText));
            }
        }
    }

    private static void applyStringConstraints(ObjectNode schemaObject, Map<String, String> keyValues) {
        if (keyValues.containsKey("length")) {
            int n = parseInt(normalizeNumericLiteral(keyValues.get("length")));
            schemaObject.put(JsonSchemaConstants.K_MIN_LENGTH, n);
            schemaObject.put(JsonSchemaConstants.K_MAX_LENGTH, n);
        }
        if (keyValues.containsKey("minLength")) {
            schemaObject.put(JsonSchemaConstants.K_MIN_LENGTH,
                    parseInt(normalizeNumericLiteral(keyValues.get("minLength"))));
        }
        if (keyValues.containsKey("maxLength")) {
            schemaObject.put(JsonSchemaConstants.K_MAX_LENGTH,
                    parseInt(normalizeNumericLiteral(keyValues.get("maxLength"))));
        }
        if (keyValues.containsKey("pattern")) {
            schemaObject.put(JsonSchemaConstants.K_PATTERN, stripRegexOrQuotes(keyValues.get("pattern")));
        }
    }

    private static void applyArrayConstraints(ObjectNode schemaObject, Map<String, String> keyValues) {
        if (keyValues.containsKey("length")) {
            int n = parseInt(normalizeNumericLiteral(keyValues.get("length")));
            schemaObject.put(JsonSchemaConstants.K_MIN_ITEMS, n);
            schemaObject.put(JsonSchemaConstants.K_MAX_ITEMS, n);
        }
        if (keyValues.containsKey("minLength")) {
            schemaObject.put(JsonSchemaConstants.K_MIN_ITEMS,
                    parseInt(normalizeNumericLiteral(keyValues.get("minLength"))));
        }
        if (keyValues.containsKey("maxLength")) {
            schemaObject.put(JsonSchemaConstants.K_MAX_ITEMS,
                    parseInt(normalizeNumericLiteral(keyValues.get("maxLength"))));
        }
    }

    private static boolean parseBooleanOrDefault(String text, boolean defaultValue) {
        if (text == null) return defaultValue;
        String s = text.trim();
        if ("true".equalsIgnoreCase(s)) return true;
        if ("false".equalsIgnoreCase(s)) return false;
        return defaultValue;
    }

    private static String normalizeNumericLiteral(String source) {
        // Remove underscores and non-number suffixes; keep sign and decimal point
        return source.replace("_", "").replaceAll("[^0-9+\\-.]", "");
    }

    private static int parseInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException nfe) {
            // Fallback if someone provided a decimal for a length
            return new BigDecimal(s).intValue();
        }
    }

    private static String stripRegexOrQuotes(String raw) {
        String s = raw.trim();
        // re`...`
        if (s.startsWith("re`") && s.endsWith("`")) return s.substring(3, s.length() - 1);
        // "..."/'...'
        if ((s.startsWith("\"") && s.endsWith("\"")) || (s.startsWith("'") && s.endsWith("'"))) {
            return s.substring(1, s.length() - 1);
        }
        return s;
    }

    private static Optional<String> optionalTypeName(TypeSymbol typeSymbol) {
        try {
            // TypeSymbol.getName() is Optional<String> in modern APIs
            Method getNameMethod = typeSymbol.getClass().getMethod("getName");
            Object val = getNameMethod.invoke(typeSymbol);
            if (val instanceof Optional<?> opt && opt.isPresent() && opt.get() instanceof String s) {
                return Optional.of(s);
            }
        } catch (ReflectiveOperationException ignore) {
            // fall through
        }
        return Optional.empty();
    }

    /** Prefer MapTypeSymbol#typeParam(); fall back to deprecated #typeParameter(). */
    private static TypeSymbol resolveMapValueType(MapTypeSymbol mapTypeSymbol) {
        try {
            Method typeParamMethod = mapTypeSymbol.getClass().getMethod("typeParam");
            Object invocationResult = typeParamMethod.invoke(mapTypeSymbol);
            if (invocationResult instanceof TypeSymbol typeSymbol) return typeSymbol;
        } catch (ReflectiveOperationException ignore) { /* fall back */ }
        try {
            Method typeParameterMethod = mapTypeSymbol.getClass().getMethod("typeParameter");
            Object invocationResult = typeParameterMethod.invoke(mapTypeSymbol);
            if (invocationResult instanceof java.util.Optional<?> optional
                    && optional.isPresent()
                    && optional.get() instanceof TypeSymbol typeSymbol) {
                return typeSymbol;
            }
        } catch (ReflectiveOperationException ignore) { /* give up */ }
        return null;
    }

    private static JsonSchemaNode mapBasicTypeToSchema(TypeSymbol ballerinaTypeSymbol) {
        BallerinaBasicType ballerinaBasicType = switch (ballerinaTypeSymbol.typeKind()) {
            case INT -> BallerinaBasicType.INT;
            case FLOAT -> BallerinaBasicType.FLOAT;
            case DECIMAL -> BallerinaBasicType.DECIMAL;
            case STRING -> BallerinaBasicType.STRING;
            case BOOLEAN -> BallerinaBasicType.BOOLEAN;
            case NIL -> BallerinaBasicType.NULL;
            default -> null;
        };
        if (ballerinaBasicType == null) {
            throw new IllegalArgumentException("Not a basic primitive type: " + ballerinaTypeSymbol.typeKind());
        }
        return BasicTypeMapper.forBasicType(ballerinaBasicType);
    }

    private static boolean isNilType(TypeSymbol ballerinaTypeSymbol) {
        return dereferenceTypeReferences(ballerinaTypeSymbol).typeKind() == TypeDescKind.NIL;
    }

    /** If schema has `"type": "<x>"`, rewrite to `["<x>","null"]`. */
    private static void makeTypeNullableInPlace(JsonSchemaNode schemaNode) {
        ObjectNode objectNode = schemaNode.toObjectNode();
        if (!objectNode.has(JsonSchemaConstants.K_TYPE)) return;

        if (objectNode.get(JsonSchemaConstants.K_TYPE).isTextual()) {
            String originalType = objectNode.get(JsonSchemaConstants.K_TYPE).asText();
            ArrayNode typeArrayNode = JsonSchemaNode.mapper().createArrayNode();
            typeArrayNode.add(originalType);
            typeArrayNode.add(JsonSchemaConstants.T_NULL);
            objectNode.set(JsonSchemaConstants.K_TYPE, typeArrayNode);
        } else if (objectNode.get(JsonSchemaConstants.K_TYPE).isArray()) {
            ArrayNode typeArrayNode = (ArrayNode) objectNode.get(JsonSchemaConstants.K_TYPE);
            boolean hasNullAlready = false;
            for (int i = 0; i < typeArrayNode.size(); i++) {
                if (typeArrayNode.get(i).isTextual()
                        && JsonSchemaConstants.T_NULL.equals(typeArrayNode.get(i).asText())) {
                    hasNullAlready = true;
                    break;
                }
            }
            if (!hasNullAlready) typeArrayNode.add(JsonSchemaConstants.T_NULL);
        }
    }

    /** Follow type references to the underlying descriptor. */
    private static TypeSymbol dereferenceTypeReferences(TypeSymbol typeSymbol) {
        while (typeSymbol.typeKind() == TypeDescKind.TYPE_REFERENCE) {
            typeSymbol = ((TypeReferenceTypeSymbol) typeSymbol).typeDescriptor();
        }
        return typeSymbol;
    }

    private static JsonSchemaNode wrap(ObjectNode objectNode) {
        JsonSchemaNode schemaNode = JsonSchemaNode.empty();
        schemaNode.toObjectNode().setAll(objectNode);
        return schemaNode;
    }
}
