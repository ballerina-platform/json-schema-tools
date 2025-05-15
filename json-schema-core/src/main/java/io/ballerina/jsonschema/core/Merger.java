//package io.ballerina.jsonschema.core;
//
//import java.util.Arrays;
//import java.util.Objects;
//import java.util.function.BiConsumer;
//import java.util.function.Function;
//
//public class Merger {
//    private static class MergeConfig {
//        boolean annotRequired;
//        Schema mainSchema;
//        Schema subSchema;
//
//        Schema providedMainSchema;
//        Schema providedSubSchema;
//
//        MergeConfig(Schema schema) {
//            annotRequired = false;
//            this.mainSchema = schema;
//        }
//
//        MergeConfig(boolean bool) {
//            annotRequired = bool;
//        }
//
//        MergeConfig(Schema main, Schema sub) {
//            annotRequired = false;
//            mainSchema = new Schema();
//            subSchema = new Schema();
//            providedMainSchema = main;
//            providedSubSchema = sub;
//        }
//
//        public <T> void copyFromBoth(Function<Schema, T> getter, BiConsumer<Schema, T> setter) {
//            annotRequired = true;
//            T mainValue = getter.apply(providedMainSchema);
//            T subValue = getter.apply(providedSubSchema);
//            setter.accept(mainSchema, mainValue);
//            setter.accept(subSchema, subValue);
//        }
//
//        public Schema getMainSchema() {
//            return mainSchema;
//        }
//
//        public Schema getSubSchema() {
//            return subSchema;
//        }
//    }
//
//    MergeConfig mergeSchema(Object mainObject, Object subObject) {
//        // Handling the case where mainSchemaObject or subSchemaObject is a boolean
//        if (mainObject instanceof Boolean && subObject instanceof Schema sub) {
//            return new MergeConfig(sub);
//        }
//        if (mainObject instanceof Schema main && subObject instanceof Boolean) {
//            return new MergeConfig(main);
//        }
//        if (mainObject instanceof Boolean main && subObject instanceof Boolean sub) {
//            return new MergeConfig(main && sub);
//        }
//
//        assert mainObject instanceof Schema;
//        assert subObject instanceof Schema;
//
//        Schema main = (Schema) mainObject;
//        Schema sub = (Schema) subObject;
//
//        MergeConfig mergeConfig = new MergeConfig(main, sub);
//        Schema mainSchema = mergeConfig.getMainSchema();
//        Schema subSchema = mergeConfig.getSubSchema();
//
//        // Number
//        if (main.getMaximum() != null) {
//            if (sub.getMaximum() != null) {
//                mainSchema.setMaximum(Math.min(main.getMaximum(), sub.getMaximum()));
//            } else {
//                mainSchema.setMaximum(main.getMaximum());
//            }
//        } else {
//            mainSchema.setMaximum(sub.getMaximum());
//        }
//
//        if (main.getMinimum() != null) {
//            if (sub.getMinimum() != null) {
//                mainSchema.setMinimum(Math.max(main.getMinimum(), sub.getMinimum()));
//            } else {
//                mainSchema.setMinimum(main.getMinimum());
//            }
//        } else {
//            mainSchema.setMinimum(sub.getMinimum());
//        }
//
//        if (main.getExclusiveMaximum() != null) {
//            if (sub.getExclusiveMaximum() != null) {
//                mainSchema.setExclusiveMaximum(Math.min(main.getExclusiveMaximum(), sub.getExclusiveMaximum()));
//            } else {
//                mainSchema.setExclusiveMaximum(main.getExclusiveMaximum());
//            }
//        } else {
//            mainSchema.setExclusiveMaximum(sub.getExclusiveMaximum());
//        }
//
//        if (main.getExclusiveMinimum() != null) {
//            if (sub.getExclusiveMinimum() != null) {
//                mainSchema.setExclusiveMinimum(Math.max(main.getExclusiveMinimum(), sub.getExclusiveMinimum()));
//            } else {
//                mainSchema.setExclusiveMinimum(main.getExclusiveMinimum());
//            }
//        } else {
//            mainSchema.setExclusiveMinimum(sub.getExclusiveMinimum());
//        }
//
//        if (main.getMultipleOf() != null) {
//            if (sub.getMultipleOf() != null) {
//                mergeConfig.copyFromBoth(Schema::getMultipleOf, Schema::setMultipleOf);
//            } else {
//                mainSchema.setMultipleOf(main.getMultipleOf());
//            }
//        } else {
//            mainSchema.setMultipleOf(sub.getMultipleOf());
//        }
//
//        // String
//        if (main.getMinLength() != null) {
//            if (sub.getMinLength() != null) {
//                mainSchema.setMinLength(Math.max(main.getMinLength(), sub.getMinLength()));
//            } else {
//                mainSchema.setMinLength(main.getMinLength());
//            }
//        } else {
//            mainSchema.setMinLength(sub.getMinLength());
//        }
//
//        if (main.getMaxLength() != null) {
//            if (sub.getMaxLength() != null) {
//                mainSchema.setMaxLength(Math.min(main.getMaxLength(), sub.getMaxLength()));
//            } else {
//                mainSchema.setMaxLength(main.getMaxLength());
//            }
//        } else {
//            mainSchema.setMaxLength(sub.getMaxLength());
//        }
//
//        if (main.getPattern() != null) {
//            if (sub.getPattern() != null) {
//                mergeConfig.copyFromBoth(Schema::getPattern, Schema::setPattern);
//            } else {
//                mainSchema.setPattern(main.getPattern());
//            }
//        } else {
//            mainSchema.setPattern(sub.getPattern());
//        }
//
//        if (main.getFormat() != null) {
//            if (sub.getFormat() != null) {
//                mergeConfig.copyFromBoth(Schema::getFormat, Schema::setFormat);
//            } else {
//                mainSchema.setFormat(main.getFormat());
//            }
//        } else {
//            mainSchema.setFormat(sub.getFormat());
//        }
//
//        if (areAllNull(main.getContentEncoding(), main.getContentMediaType(), main.getContentSchema())) {
//            if (areAllNull(sub.getContentEncoding(), sub.getContentMediaType(), sub.getContentSchema())) {
//            mergeConfig.copyFromBoth(Schema::getContentEncoding, Schema::setContentEncoding);
//            mergeConfig.copyFromBoth(Schema::getContentMediaType, Schema::setContentMediaType);
//            mergeConfig.copyFromBoth(Schema::getContentSchema, Schema::setContentSchema);
//            } else {
//                mainSchema.setContentEncoding(main.getContentEncoding());
//                mainSchema.setContentMediaType(main.getContentMediaType());
//                mainSchema.setContentSchema(main.getContentSchema());
//            }
//        } else {
//            mainSchema.setContentEncoding(sub.getContentEncoding());
//            mainSchema.setContentMediaType(sub.getContentMediaType());
//            mainSchema.setContentSchema(sub.getContentSchema());
//        }
//
//        //TODO: Complete this code.
//
//        return mergeConfig;
//    }
//
//    static boolean areAllNull(Object... objects) {
//        return Arrays.stream(objects).allMatch(Objects::isNull);
//    }
//}
