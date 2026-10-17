package com.worldsreborn.marvel.doomsday;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/** A reception supply package (Scene 7): water, blankets or medical supplies, with who it is for. */
public class DeliveryItem extends Item {
    private final String recipientKey;

    public DeliveryItem(String recipientKey, Properties properties) {
        super(properties);
        this.recipientKey = recipientKey;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(recipientKey));
    }
}
