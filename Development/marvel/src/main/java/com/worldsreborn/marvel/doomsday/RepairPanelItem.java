package com.worldsreborn.marvel.doomsday;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;

/**
 * TVA emergency repair panels (Scenes 7-9). They only go down inside the marked construction zones, so a repair can
 * never wall off the set or bury the people Steve is helping.
 */
public class RepairPanelItem extends BlockItem {
    public RepairPanelItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult place(BlockPlaceContext context) {
        if (!DoomsdaySites.inConstructionZone(context.getLevel(), context.getClickedPos())) {
            if (context.getPlayer() != null && !context.getLevel().isClientSide) {
                context.getPlayer().displayClientMessage(Component.translatable("message.wrmarvel.outside_zone"), true);
            }
            return InteractionResult.FAIL;
        }
        return super.place(context);
    }
}
