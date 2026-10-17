package com.worldsreborn.marvel.item;

import com.worldsreborn.marvel.MarvelStory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** One of the cartographer's two surveys of the valley (Scene 1). Using it unrolls the map. */
public class SurveyMapItem extends Item {
    private final boolean newSurvey;

    public SurveyMapItem(boolean newSurvey, Properties properties) {
        super(properties);
        this.newSurvey = newSurvey;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (player instanceof ServerPlayer sp) {
            MarvelStory.onSurveyRead(sp, newSurvey);
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
    }
}
