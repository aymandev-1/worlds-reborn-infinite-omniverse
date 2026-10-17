package com.worldsreborn.core.model;

/** Dimension + position, MC-free so it can live in the save model. */
public record Location(String dimension, double x, double y, double z, float yaw, float pitch) {
    public Location(String dimension, double x, double y, double z) {
        this(dimension, x, y, z, 0f, 0f);
    }

    public double distanceSq(String dim, double px, double py, double pz) {
        if (!dimension.equals(dim)) {
            return Double.MAX_VALUE;
        }
        double dx = x - px, dy = y - py, dz = z - pz;
        return dx * dx + dy * dy + dz * dz;
    }
}
