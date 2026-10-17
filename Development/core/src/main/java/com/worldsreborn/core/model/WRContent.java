package com.worldsreborn.core.model;

/**
 * Global content registries shared by every Worlds Reborn module. Content mods register during mod construction,
 * and the registries are frozen at common setup. Identical on client and server because both load the same mods.
 */
public final class WRContent {
    public static final ContentRegistry<UniverseInfo> UNIVERSES = new ContentRegistry<>("universe", u -> "universe:" + u.id());
    public static final ContentRegistry<QuestDef> QUESTS = new ContentRegistry<>("quest", QuestDef::id);
    public static final ContentRegistry<DialogueDef> DIALOGUES = new ContentRegistry<>("dialogue", DialogueDef::id);
    public static final ContentRegistry<Destination> DESTINATIONS = new ContentRegistry<>("destination", Destination::id);
    public static final ContentRegistry<AbilityDef> ABILITIES = new ContentRegistry<>("ability", AbilityDef::id);

    private WRContent() {
    }

    public static UniverseInfo universe(UniverseId id) {
        return UNIVERSES.getOrNull("universe:" + id);
    }

    public static void freezeAll() {
        UNIVERSES.freeze();
        QUESTS.freeze();
        DIALOGUES.freeze();
        DESTINATIONS.freeze();
        ABILITIES.freeze();
    }
}
