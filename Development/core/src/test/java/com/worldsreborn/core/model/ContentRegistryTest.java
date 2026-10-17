package com.worldsreborn.core.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ContentRegistryTest {
    @Test
    void rejectsDuplicatesUnnamespacedAndLateRegistration() {
        ContentRegistry<DialogueDef> reg = new ContentRegistry<>("dialogue", DialogueDef::id);
        DialogueDef d = new DialogueDef("t:hello", List.of(new DialogueLine("s", "t", true)), List.of());
        reg.register(d);
        assertThrows(IllegalArgumentException.class, () -> reg.register(d));
        assertThrows(IllegalArgumentException.class,
                () -> reg.register(new DialogueDef("plain", List.of(new DialogueLine("s", "t", true)), List.of())));
        reg.freeze();
        assertThrows(IllegalStateException.class,
                () -> reg.register(new DialogueDef("t:late", List.of(new DialogueLine("s", "t", true)), List.of())));
        assertSame(d, reg.getOrNull("t:hello"));
    }
}
