package com.leecrafts.bowmaster.event;

import com.leecrafts.bowmaster.SkeletonBowMaster;
import com.leecrafts.bowmaster.capability.ModCapabilities;
import com.leecrafts.bowmaster.capability.player.IPlayerCap;
import com.leecrafts.bowmaster.capability.player.PlayerCap;
import com.leecrafts.bowmaster.capability.player.PlayerCapProvider;
import com.leecrafts.bowmaster.entity.ModEntityTypes;
import com.leecrafts.bowmaster.entity.client.SkeletonBowMasterModel;
import com.leecrafts.bowmaster.entity.custom.SkeletonBowMasterEntity;
import com.leecrafts.bowmaster.world.dimension.ModDimensions;
import com.leecrafts.bowmaster.world.portal.ModTeleporter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

public class ModEvents {

    @Mod.EventBusSubscriber(modid = SkeletonBowMaster.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ForgeEventBusEvents {

        @SubscribeEvent
        public static void registerCapabilities(RegisterCapabilitiesEvent event) {
            event.register(IPlayerCap.class);
        }

        @SubscribeEvent
        public static void onAttachCapabilitiesEventPlayer(AttachCapabilitiesEvent<Entity> event) {
            if (event.getObject() instanceof Player player && !player.getCommandSenderWorld().isClientSide) {
                if (!player.getCapability(ModCapabilities.PLAYER_CAPABILITY).isPresent()) {
                    PlayerCapProvider playerCapProvider = new PlayerCapProvider();
                    event.addCapability(new ResourceLocation(SkeletonBowMaster.MODID, "player"), playerCapProvider);
                }
            }
        }

        @SubscribeEvent
        public static void hurtEntity(LivingHurtEvent event) {
            LivingEntity livingEntity = event.getEntity();
            if (!livingEntity.level().isClientSide && event.getSource().getEntity() instanceof SkeletonBowMasterEntity skeletonBowMasterEntity) {
                skeletonBowMasterEntity.increaseReward(event.getAmount());
                if (livingEntity instanceof Player player) {
                    ModTeleporter.teleportPlayer(player);
                }
            }
        }

        @SubscribeEvent
        public static void onPlayerCloneEvent(PlayerEvent.Clone event) {
            Player originalPlayer = event.getOriginal();
            Player player = event.getEntity();
            originalPlayer.reviveCaps();
            originalPlayer.getCapability(ModCapabilities.PLAYER_CAPABILITY).ifPresent(iPlayerCap -> {
                player.getCapability(ModCapabilities.PLAYER_CAPABILITY).ifPresent(iPlayerCap1 -> {
                    PlayerCap playerCap = (PlayerCap) iPlayerCap;
                    PlayerCap playerCap1 = (PlayerCap) iPlayerCap1;
                    playerCap1.outsideDimBlockPos = playerCap.outsideDimBlockPos;
                    playerCap1.arenaDimBlockPos = playerCap.arenaDimBlockPos;
                });
            });
            originalPlayer.invalidateCaps();
        }

    }

    @Mod.EventBusSubscriber(modid = SkeletonBowMaster.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModEventBusEvents {

        @SubscribeEvent
        public static void registerAttributes(EntityAttributeCreationEvent event) {
            event.put(ModEntityTypes.SKELETON_BOW_MASTER.get(), SkeletonBowMasterEntity.createAttributes().build());
        }

    }

    @Mod.EventBusSubscriber(modid = SkeletonBowMaster.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ModEventBusClientEvents {

        @SubscribeEvent
        public static void registerLayer(EntityRenderersEvent.RegisterLayerDefinitions event) {
            event.registerLayerDefinition(SkeletonBowMasterModel.LAYER_LOCATION, SkeletonBowMasterModel::createBodyLayer);
        }

    }

}
