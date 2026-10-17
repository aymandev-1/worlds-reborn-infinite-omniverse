package com.worldsreborn.core.model;

/**
 * A place players can travel to.
 *
 * @param universe        universe id string (e.g. "MU-616"), or null for places outside ordinary universes (TVA)
 * @param dimension       dimension resource location string, e.g. "minecraft:overworld"
 * @param unlockQuestId   quest whose completion unlocks this destination, or null if unlocked explicitly
 * @param hiddenUntilDiscovered show only "???" until the player has been there
 */
public record Destination(String id, String universe, String dimension, String nameKey, String descriptionKey,
                          String unlockQuestId, boolean unlockedByDefault, boolean hiddenUntilDiscovered,
                          int arrivalX, int arrivalY, int arrivalZ) {
}
