package com.worldsreborn.marvel.item;

import com.worldsreborn.marvel.WRMarvel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class MarvelItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(WRMarvel.MODID);

    public static final DeferredItem<SurveyMapItem> OLD_SURVEY = ITEMS.register("old_survey",
            () -> new SurveyMapItem(false, new Item.Properties().stacksTo(1)));
    public static final DeferredItem<SurveyMapItem> NEW_SURVEY = ITEMS.register("new_survey",
            () -> new SurveyMapItem(true, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));

    // ---- Act 1 Scenes 6-11
    public static final DeferredItem<com.worldsreborn.marvel.doomsday.DeliveryItem> WATER = ITEMS.register("water_crate",
            () -> new com.worldsreborn.marvel.doomsday.DeliveryItem("item.wrmarvel.water_crate.for", new Item.Properties().stacksTo(1)));
    public static final DeferredItem<com.worldsreborn.marvel.doomsday.DeliveryItem> BLANKETS = ITEMS.register("blanket_bundle",
            () -> new com.worldsreborn.marvel.doomsday.DeliveryItem("item.wrmarvel.blanket_bundle.for", new Item.Properties().stacksTo(1)));
    public static final DeferredItem<com.worldsreborn.marvel.doomsday.DeliveryItem> MEDICAL = ITEMS.register("medical_supplies",
            () -> new com.worldsreborn.marvel.doomsday.DeliveryItem("item.wrmarvel.medical_supplies.for", new Item.Properties().stacksTo(1)));
    public static final DeferredItem<com.worldsreborn.marvel.doomsday.RepairPanelItem> REPAIR_PANEL = ITEMS.register("repair_panel",
            () -> new com.worldsreborn.marvel.doomsday.RepairPanelItem(com.worldsreborn.marvel.block.MarvelBlocks.REPAIR_PANEL.get(), new Item.Properties()));
    public static final DeferredItem<com.worldsreborn.marvel.doomsday.WebShooterItem> WEB_SHOOTERS = ITEMS.register("web_shooters",
            () -> new com.worldsreborn.marvel.doomsday.WebShooterItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<com.worldsreborn.marvel.doomsday.ScannerItem> PETER_SCANNER = ITEMS.register("peter_scanner",
            () -> new com.worldsreborn.marvel.doomsday.ScannerItem(new Item.Properties().stacksTo(1)));
    /** A disturbance recorder carried out of a damaged facility (Scenes 13 and 16). */
    public static final DeferredItem<com.worldsreborn.marvel.doomsday.DeliveryItem> MONITORING_RECORDER = ITEMS.register("monitoring_recorder",
            () -> new com.worldsreborn.marvel.doomsday.DeliveryItem("item.wrmarvel.monitoring_recorder.for", new Item.Properties().stacksTo(1)));

    private MarvelItems() {
    }
}
