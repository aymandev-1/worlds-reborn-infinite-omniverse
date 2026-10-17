package com.worldsreborn.characters;

import com.worldsreborn.core.model.ContentRegistry;

/** Character registry. Campaigns register their cast during mod construction. */
public final class Characters {
    public static final ContentRegistry<CharacterDef> REGISTRY = new ContentRegistry<>("character", CharacterDef::id);

    private Characters() {
    }

    public static CharacterDef register(CharacterDef def) {
        return REGISTRY.register(def);
    }

    public static CharacterDef get(String id) {
        return REGISTRY.getOrNull(id);
    }
}
