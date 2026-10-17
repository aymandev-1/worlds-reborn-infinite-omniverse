package com.worldsreborn.characters;

import net.minecraft.resources.ResourceLocation;

/**
 * A character: display name, skin and model shape.
 *
 * @param texture   skin texture, 64x64 (player layout for HUMANOID, villager layout for VILLAGER)
 * @param temporary          true while the skin is placeholder art awaiting final models
 * @param villagerProfession for VILLAGER models: vanilla profession whose clothes are drawn on top (e.g.
 *                           "minecraft:cartographer"), or null
 * @param heldItem           item shown in the main hand (e.g. a TVA baton), or null
 */
public record CharacterDef(String id, String nameKey, ResourceLocation texture, Model model, boolean temporary,
                           String villagerProfession, ResourceLocation heldItem) {
    public CharacterDef(String id, String nameKey, ResourceLocation texture, Model model, boolean temporary) {
        this(id, nameKey, texture, model, temporary, null, null);
    }

    public enum Model {
        HUMANOID,
        VILLAGER
    }
}
