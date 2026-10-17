package com.worldsreborn.core.model;

/**
 * Registry entry describing a universe.
 *
 * @param tvaRegistered whether the Marvel TVA has this universe in its records. MC-616 is deliberately NOT registered:
 *                      the TVA scan reads "ORIGIN UNIVERSE: UNREGISTERED", which does not mean the universe was destroyed.
 */
public record UniverseInfo(UniverseId id, String nameKey, String franchise, boolean tvaRegistered) {
}
