package com.leecrafts.bowmaster.packet;

import com.leecrafts.bowmaster.capability.ModCapabilities;
import com.leecrafts.bowmaster.capability.livingentity.LivingEntityCap;
import com.leecrafts.bowmaster.entity.custom.SkeletonBowMasterEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.network.CustomPayloadEvent;

public class ServerboundLivingEntityVelocityPacket {

    public final int id;
    public final Vec3 velocity;

    public ServerboundLivingEntityVelocityPacket(int id, Vec3 velocity) {
        this.id = id;
        this.velocity = velocity;
    }

    public ServerboundLivingEntityVelocityPacket(FriendlyByteBuf buffer) {
        this(buffer.readInt(), buffer.readVec3());
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeInt(this.id);
        buffer.writeVec3(this.velocity);
    }

    public void handle(CustomPayloadEvent.Context ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer sender = ctx.getSender();
            if (sender != null) {
                Entity entity = sender.level().getEntity(this.id);
                if (entity instanceof LivingEntity) {
                    entity.getCapability(ModCapabilities.LIVING_ENTITY_CAPABILITY).ifPresent(iLivingEntityCap -> {
                        LivingEntityCap livingEntityCap = (LivingEntityCap) iLivingEntityCap;
                        livingEntityCap.setVelocity(this.velocity);
                    });
                }
            }
        });
        ctx.setPacketHandled(true);
    }
}
