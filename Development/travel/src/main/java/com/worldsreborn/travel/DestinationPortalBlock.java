package com.worldsreborn.travel;

import com.worldsreborn.travel.event.PortalContactEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.NeoForge;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A walk-through portal leading to a destination. Campaigns decide what contact means (story gating, cinematic) by
 * handling {@link PortalContactEvent}; unhandled contact performs plain travel if the destination is unlocked.
 */
public class DestinationPortalBlock extends Block {
    private static final Map<UUID, Long> LAST_CONTACT = new ConcurrentHashMap<>();
    private static final int CONTACT_COOLDOWN_TICKS = 60;

    private final String destinationId;

    public DestinationPortalBlock(String destinationId, Properties properties) {
        super(properties);
        this.destinationId = destinationId;
    }

    public String getDestinationId() {
        return destinationId;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (level.isClientSide || !(entity instanceof ServerPlayer player)) {
            return;
        }
        long now = level.getGameTime();
        Long last = LAST_CONTACT.get(player.getUUID());
        if (last != null && now - last < CONTACT_COOLDOWN_TICKS) {
            return;
        }
        LAST_CONTACT.put(player.getUUID(), now);
        PortalContactEvent event = NeoForge.EVENT_BUS.post(new PortalContactEvent(player, destinationId, pos));
        if (!event.isHandled()) {
            var d = TravelService.destination(destinationId);
            if (d != null && TravelService.isUnlocked(player, d)) {
                TravelService.beginTransit(player, destinationId, null, 40);
            }
        }
    }
}
