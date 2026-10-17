package com.worldsreborn.core.model;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Stable identifier for a universe, written as {@code MULTIVERSE-DESIGNATION}, e.g. {@code MC-616} or {@code MU-616}.
 * The multiverse prefix is part of the identity: MC-616 (Minecraft) and MU-616 (Marvel) are different universes.
 */
public record UniverseId(String multiverse, String designation) {
    private static final Pattern MULTIVERSE = Pattern.compile("[A-Z]{2,8}");
    private static final Pattern DESIGNATION = Pattern.compile("[A-Z0-9]{1,16}");

    public UniverseId {
        Objects.requireNonNull(multiverse, "multiverse");
        Objects.requireNonNull(designation, "designation");
        if (!MULTIVERSE.matcher(multiverse).matches()) {
            throw new IllegalArgumentException("Invalid multiverse prefix: " + multiverse);
        }
        if (!DESIGNATION.matcher(designation).matches()) {
            throw new IllegalArgumentException("Invalid universe designation: " + designation);
        }
    }

    public static UniverseId parse(String text) {
        int dash = text.indexOf('-');
        if (dash <= 0 || dash == text.length() - 1) {
            throw new IllegalArgumentException("Universe id must look like MC-616: " + text);
        }
        return new UniverseId(text.substring(0, dash), text.substring(dash + 1));
    }

    @Override
    public String toString() {
        return multiverse + "-" + designation;
    }
}
