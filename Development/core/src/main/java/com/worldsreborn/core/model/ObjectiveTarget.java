package com.worldsreborn.core.model;

/** Where the currently tracked objective points. Used for REACH steps and the HUD distance hint. */
public record ObjectiveTarget(String questId, String stepId, Location location, double radius) {
}
