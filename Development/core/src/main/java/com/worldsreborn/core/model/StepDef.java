package com.worldsreborn.core.model;

public record StepDef(String id, String descriptionKey, StepType type, int required) {
    public StepDef {
        if (required < 1) {
            throw new IllegalArgumentException("required must be >= 1");
        }
    }

    public static StepDef action(String id, String descriptionKey) {
        return new StepDef(id, descriptionKey, StepType.ACTION, 1);
    }

    public static StepDef reach(String id, String descriptionKey) {
        return new StepDef(id, descriptionKey, StepType.REACH, 1);
    }

    public static StepDef count(String id, String descriptionKey, int required) {
        return new StepDef(id, descriptionKey, StepType.COUNT, required);
    }
}
