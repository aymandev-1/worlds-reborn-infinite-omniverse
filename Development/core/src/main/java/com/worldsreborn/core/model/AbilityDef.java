package com.worldsreborn.core.model;

/** Static definition of an ability. Effects are implemented by the owning module when the ability fires. */
public record AbilityDef(String id, String nameKey, String descriptionKey, int cooldownTicks, String owner) {
}
