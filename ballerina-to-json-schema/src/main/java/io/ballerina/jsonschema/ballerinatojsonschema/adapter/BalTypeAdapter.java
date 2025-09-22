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
import io.ballerina.jsonschema.ballerinatojsonschema.BallerinaBasicType;
import io.ballerina.jsonschema.ballerinatojsonschema.BasicTypeMapper;
import io.ballerina.jsonschema.ballerinatojsonschema.JsonSchemaConstants;
import io.ballerina.jsonschema.ballerinatojsonschema.JsonSchemaNode;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;

public final class BalTypeAdapter {
    private BalTypeAdapter() {}

    /** Stage 1 + Stage 2: primitives, arrays, records (and map&lt;T&gt;). */
    public static JsonSchemaNode toSchemaNode(TypeSymbol typeSymbol, boolean allowOptionalNull) {
        typeSymbol = deref(typeSymbol);

        if (allowOptionalNull && typeSymbol.typeKind() == TypeDescKind.UNION) {
            UnionTypeSymbol unionTypeSymbol = (UnionTypeSymbol) typeSymbol;
            if (unionTypeSymbol.memberTypeDescriptors().size() == 2) {
                TypeSymbol a = deref(unionTypeSymbol.memberTypeDescriptors().get(0));
                TypeSymbol b = deref(unionTypeSymbol.memberTypeDescriptors().get(1));
                TypeSymbol nonNil = isNil(a) ? b : (isNil(b) ? a : null);
                if (nonNil != null) {
                    JsonSchemaNode base = toSchemaNode(nonNil, /*allowOptionalNull*/ false);
                    makeNullableType(base);
                    return base;
                }
            }
            throw new IllegalArgumentException("Unsupported union" + unionTypeSymbol);
        }

        return switch (typeSymbol.typeKind()) {
            case INT, FLOAT, DECIMAL, STRING, BOOLEAN, NIL -> toBasicType(typeSymbol);
            case ARRAY -> arraySchema((ArrayTypeSymbol) typeSymbol, allowOptionalNull);
            case RECORD -> recordSchema((RecordTypeSymbol) typeSymbol, allowOptionalNull);
            case MAP -> mapSchema((MapTypeSymbol) typeSymbol, allowOptionalNull);
            default -> throw new
                    IllegalArgumentException("Unsupported Ballerina type: " + typeSymbol.typeKind());
        };
    }

    private static JsonSchemaNode arraySchema(ArrayTypeSymbol arrayTypeSymbol, boolean allowOptionalNull) {
        TypeSymbol member = deref(arrayTypeSymbol.memberTypeDescriptor());
        JsonSchemaNode items = toSchemaNode(member, allowOptionalNull);
        ObjectNode schema = JsonSchemaNode.mapper().createObjectNode();
        schema.put(JsonSchemaConstants.K_TYPE, JsonSchemaConstants.T_ARRAY);
        schema.set("items", items.toObjectNode());
        return wrap(schema);
    }

    private static JsonSchemaNode recordSchema(RecordTypeSymbol recordTypeSymbol, boolean allowOptionalNull) {
        ObjectNode schema = JsonSchemaNode.mapper().createObjectNode();
        schema.put(JsonSchemaConstants.K_TYPE, JsonSchemaConstants.T_OBJECT);

        ObjectNode properties = JsonSchemaNode.mapper().createObjectNode();
        ArrayNode requiredFields = JsonSchemaNode.mapper().createArrayNode();
        boolean hasRequired = false;

        for (Map.Entry<String, RecordFieldSymbol> entry : recordTypeSymbol.fieldDescriptors().entrySet()) {
            String fieldName = entry.getKey();
            RecordFieldSymbol field = entry.getValue();
            TypeSymbol fieldType = deref(field.typeDescriptor());

            JsonSchemaNode fieldSchema = toSchemaNode(fieldType, allowOptionalNull);
            properties.set(fieldName, fieldSchema.toObjectNode());

            if (!field.isOptional()) {
                requiredFields.add(fieldName);
                hasRequired = true;
            }
        }
        schema.set("properties", properties);
        if (hasRequired) {
            schema.set("requiredFields", requiredFields);
        }

        Optional<TypeSymbol> restTypeOpt = recordTypeSymbol.restTypeDescriptor();
        if (restTypeOpt.isPresent()) {
            JsonSchemaNode additionalPropertiesSchema = toSchemaNode(deref(restTypeOpt.get()), allowOptionalNull);
            schema.set("additionalProperties", additionalPropertiesSchema.toObjectNode());
        } else {
            schema.put("additionalProperties", false);
        }

        return wrap(schema);
    }

    private static JsonSchemaNode mapSchema(MapTypeSymbol map, boolean allowOptionalNull) {
        TypeSymbol valueTs = mapValueType(map);

        ObjectNode objectNode = JsonSchemaNode.mapper().createObjectNode();
        objectNode.put(JsonSchemaConstants.K_TYPE, JsonSchemaConstants.T_OBJECT);

        if (valueTs != null) {
            JsonSchemaNode valueSchema = toSchemaNode(deref(valueTs), allowOptionalNull);
            objectNode.set("additionalProperties", valueSchema.toObjectNode());
        } else {
            objectNode.set("additionalProperties", JsonSchemaNode.mapper().createObjectNode());
        }
        return wrap(objectNode);
    }

    /** Prefer MapTypeSymbol#typeParam(); fall back to deprecated #typeParameter(). */
    private static TypeSymbol mapValueType(MapTypeSymbol map) {
        try {
            Method typeParamMethod = map.getClass().getMethod("typeParam");
            Object invocationResult = typeParamMethod.invoke(map);
            if (invocationResult instanceof TypeSymbol typeSymbol) return typeSymbol;
        } catch (ReflectiveOperationException ignore) { /* fall back */ }
        try {
            Method typeParamMethod = map.getClass().getMethod("typeParameter");
            Object invocationResult = typeParamMethod.invoke(map);
            if (invocationResult instanceof java.util.Optional<?> opt && opt.isPresent()
                    && opt.get() instanceof TypeSymbol typeSymbol) {
                return typeSymbol;
            }
        } catch (ReflectiveOperationException ignore) { /* give up */ }

        return null;
    }

    private static JsonSchemaNode toBasicType(TypeSymbol typeSymbol) {
        BallerinaBasicType ballerinaBasicType = switch (typeSymbol.typeKind()) {
            case INT     -> BallerinaBasicType.INT;
            case FLOAT   -> BallerinaBasicType.FLOAT;
            case DECIMAL -> BallerinaBasicType.DECIMAL;
            case STRING  -> BallerinaBasicType.STRING;
            case BOOLEAN -> BallerinaBasicType.BOOLEAN;
            case NIL     -> BallerinaBasicType.NULL;
            default      -> null;
        };
        if (ballerinaBasicType == null)
            throw new IllegalArgumentException("Not a basic type primitive: " + typeSymbol.typeKind());
        return BasicTypeMapper.forBasicType(ballerinaBasicType);
    }

    private static boolean isNil(TypeSymbol typeSymbol) {
        return deref(typeSymbol).typeKind() == TypeDescKind.NIL;
    }

    /** If schema has `"type": "<x>"`, rewrites it to `["<x>","null"]`. */
    private static void makeNullableType(JsonSchemaNode jsonSchemaNode) {
        ObjectNode objectNode = jsonSchemaNode.toObjectNode();
        if (!objectNode.has(JsonSchemaConstants.K_TYPE)) return;

        if (objectNode.get(JsonSchemaConstants.K_TYPE).isTextual()) {
            String baseType = objectNode.get(JsonSchemaConstants.K_TYPE).asText();
            ArrayNode arrayNode = JsonSchemaNode.mapper().createArrayNode();
            arrayNode.add(baseType);
            arrayNode.add(JsonSchemaConstants.T_NULL);
            objectNode.set(JsonSchemaConstants.K_TYPE, arrayNode);
        } else if (objectNode.get(JsonSchemaConstants.K_TYPE).isArray()) {
            ArrayNode arrayNode = (ArrayNode) objectNode.get(JsonSchemaConstants.K_TYPE);
            boolean hasNull = false;
            for (int i = 0; i < arrayNode.size(); i++) {
                if (arrayNode.get(i).isTextual() && JsonSchemaConstants.T_NULL.equals(arrayNode.get(i).asText())) {
                    hasNull = true; break;
                }
            }
            if (!hasNull) arrayNode.add(JsonSchemaConstants.T_NULL);
        }
    }

    /** Follow type references to the underlying descriptor. */
    private static TypeSymbol deref(TypeSymbol typeSymbol) {
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

    public static JsonSchemaNode toBasicTypeSchemaNode(TypeSymbol typeSymbol, boolean allowOptionalAsTypeArray) {
        JsonSchemaNode jsonSchemaNode = toSchemaNode(typeSymbol, allowOptionalAsTypeArray);
        return switch (deref(typeSymbol).typeKind()) {
            case INT, FLOAT, DECIMAL, STRING, BOOLEAN, NIL, UNION -> jsonSchemaNode;
            default -> throw new IllegalArgumentException("Unsupported type: " + typeSymbol.typeKind());
        };
    }
}
