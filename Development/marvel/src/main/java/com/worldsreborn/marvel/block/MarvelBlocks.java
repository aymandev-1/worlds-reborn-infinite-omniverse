package com.worldsreborn.marvel.block;

import com.worldsreborn.marvel.MarvelIds;
import com.worldsreborn.marvel.WRMarvel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Story blocks. None of them drop or can be mined in survival: they are set dressing for scenes. */
public final class MarvelBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(WRMarvel.MODID);

    /** The silver-blue portal beneath the ruins (Scene 2). Leads to MU-616 New York. */
    public static final DeferredBlock<SilverPortalBlock> SILVER_PORTAL = BLOCKS.register("silver_portal",
            () -> new SilverPortalBlock(MarvelIds.DEST_NEW_YORK, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.ICE).noCollission().noLootTable().strength(-1f, 3_600_000f)
                    .lightLevel(s -> 14).sound(SoundType.GLASS).pushReaction(PushReaction.BLOCK).noOcclusion()));

    /** The block the repaired redstone line must power to open the portal. */
    public static final DeferredBlock<Block> PORTAL_CORE = BLOCKS.register("portal_core",
            () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).noLootTable()
                    .strength(-1f, 3_600_000f).lightLevel(s -> 4).sound(SoundType.DEEPSLATE)));

    /** What is left where the portal closed in New York (Scenes 3-4). */
    public static final DeferredBlock<FadingTraceBlock> FADING_TRACE = BLOCKS.register("fading_trace",
            () -> new FadingTraceBlock(BlockBehaviour.Properties.of().mapColor(MapColor.NONE).noCollission()
                    .noLootTable().strength(-1f, 3_600_000f).lightLevel(s -> 6).noOcclusion()));

    /** TVA records terminal (Scene 5). */
    public static final DeferredBlock<TvaTerminalBlock> TVA_TERMINAL = BLOCKS.register("tva_terminal",
            () -> new TvaTerminalBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE).noLootTable()
                    .strength(-1f, 3_600_000f).lightLevel(s -> 7).sound(SoundType.METAL)));

    /** The glowing orange TVA time door that opens on the rooftop (Scenes 4-5). Walk-through light, no collision. */
    public static final DeferredBlock<net.minecraft.world.level.block.HalfTransparentBlock> TVA_DOORWAY = BLOCKS.register("tva_doorway",
            () -> new net.minecraft.world.level.block.HalfTransparentBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE).noCollission().noLootTable()
                    .strength(-1f, 3_600_000f).lightLevel(s -> 15).sound(SoundType.AMETHYST).noOcclusion()));

    // ---- Act 1 Scenes 6-11
    /** The red-violet doorway that opens without authorisation in TVA reception (Scenes 7-8). */
    public static final DeferredBlock<net.minecraft.world.level.block.HalfTransparentBlock> RIFT_DOORWAY = BLOCKS.register("rift_doorway",
            () -> new net.minecraft.world.level.block.HalfTransparentBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).noCollission().noLootTable()
                    .strength(-1f, 3_600_000f).lightLevel(s -> 12).sound(SoundType.AMETHYST).noOcclusion()));
    public static final DeferredBlock<com.worldsreborn.marvel.doomsday.FallenCrateBlock> FALLEN_CRATE = BLOCKS.register("fallen_crate",
            () -> new com.worldsreborn.marvel.doomsday.FallenCrateBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).noLootTable()
                    .strength(-1f, 3_600_000f).sound(SoundType.WOOD)));
    public static final DeferredBlock<com.worldsreborn.marvel.doomsday.FallenCrateBlock> DEBRIS = BLOCKS.register("debris",
            () -> new com.worldsreborn.marvel.doomsday.FallenCrateBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).noLootTable()
                    .strength(-1f, 3_600_000f).sound(SoundType.STONE)));
    /** Player-placed TVA repair panel; breaks quickly, drops nothing (more come from the reception crew). */
    public static final DeferredBlock<Block> REPAIR_PANEL = BLOCKS.register("repair_panel",
            () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE).noLootTable()
                    .strength(0.4f, 6f).sound(SoundType.METAL)));
    public static final DeferredBlock<com.worldsreborn.marvel.doomsday.RecordingDisplayBlock> RECORDING_DISPLAY = BLOCKS.register("recording_display",
            () -> new com.worldsreborn.marvel.doomsday.RecordingDisplayBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).noLootTable()
                    .strength(-1f, 3_600_000f).lightLevel(s -> 9).sound(SoundType.GLASS)));
    public static final DeferredBlock<com.worldsreborn.marvel.doomsday.SupplyShelfBlock> SUPPLY_SHELF = BLOCKS.register("supply_shelf",
            () -> new com.worldsreborn.marvel.doomsday.SupplyShelfBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).noLootTable()
                    .strength(-1f, 3_600_000f).sound(SoundType.WOOD)));
    public static final DeferredBlock<com.worldsreborn.marvel.doomsday.WebAnchorBlock> WEB_ANCHOR = BLOCKS.register("web_anchor",
            () -> new com.worldsreborn.marvel.doomsday.WebAnchorBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).noLootTable()
                    .strength(-1f, 3_600_000f).sound(SoundType.METAL).noOcclusion()));

    private MarvelBlocks() {
    }
}
