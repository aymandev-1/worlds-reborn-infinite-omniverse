package com.worldsreborn.marvel.doomsday;

import com.worldsreborn.core.api.BlockPromptProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/**
 * The hands-on controls of Scenes 12-16: the flight route table, a door brace, the monitoring recorders, the
 * communications array (power, dish alignment, channel test, transmitter) and the recorder clamps. What using one does
 * depends on the scene and step; see {@link Responders#onControlUsed}.
 */
public class StoryControlBlock extends Block implements BlockPromptProvider {
    public enum Kind implements StringRepresentable {
        ROUTE_TABLE, BRACE, RECORDER, POWER, DISH, TEST, TRANSMIT, CLAMP;

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    public static final EnumProperty<Kind> KIND = EnumProperty.create("kind", Kind.class);

    public StoryControlBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(KIND, Kind.ROUTE_TABLE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(KIND);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide && player instanceof ServerPlayer sp) {
            Responders.onControlUsed(sp, pos, state.getValue(KIND));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    @Nullable
    public Component getInteractionPrompt(Player player, BlockState state, BlockPos pos) {
        return Component.translatable("prompt.wrmarvel.control." + state.getValue(KIND).getSerializedName());
    }
}
