package com.leecrafts.bowmaster.event;

import com.leecrafts.bowmaster.SkeletonBowMaster;
import com.leecrafts.bowmaster.entity.ModEntityTypes;
import com.leecrafts.bowmaster.entity.client.SkeletonBowMasterModel;
import com.leecrafts.bowmaster.entity.custom.SkeletonBowMasterEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

public class ModEvents {

    @Mod.EventBusSubscriber(modid = SkeletonBowMaster.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ForgeEventBusEvents {

        @SubscribeEvent
        public static void hurtEntity(LivingHurtEvent event) {
            LivingEntity livingEntity = event.getEntity();
            if (!livingEntity.level().isClientSide && event.getSource().getEntity() instanceof SkeletonBowMasterEntity skeletonBowMasterEntity) {
                skeletonBowMasterEntity.increaseReward(event.getAmount());
            }
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
