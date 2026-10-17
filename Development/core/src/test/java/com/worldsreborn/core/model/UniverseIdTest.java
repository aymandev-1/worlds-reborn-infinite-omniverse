package com.worldsreborn.core.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UniverseIdTest {
    @Test
    void minecraftAndMarvel616AreDifferentUniverses() {
        UniverseId mc = UniverseId.parse("MC-616");
        UniverseId mu = UniverseId.parse("MU-616");
        assertNotEquals(mc, mu);
        assertEquals("MC-616", mc.toString());
        assertEquals(new UniverseId("MU", "616"), mu);
    }

    @Test
    void rejectsMalformedIds() {
        assertThrows(IllegalArgumentException.class, () -> UniverseId.parse("616"));
        assertThrows(IllegalArgumentException.class, () -> UniverseId.parse("mc-616"));
        assertThrows(IllegalArgumentException.class, () -> UniverseId.parse("MC-"));
        assertDoesNotThrow(() -> UniverseId.parse("MU-NEW616"));
        assertDoesNotThrow(() -> UniverseId.parse("MU-1610B"));
    }
}
