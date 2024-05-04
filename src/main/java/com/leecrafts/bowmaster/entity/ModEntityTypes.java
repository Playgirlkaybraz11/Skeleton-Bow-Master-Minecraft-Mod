package com.leecrafts.bowmaster.entity;

import com.leecrafts.bowmaster.SkeletonBowMaster;
import com.leecrafts.bowmaster.entity.custom.SkeletonBowMasterEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntityTypes {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, SkeletonBowMaster.MODID);

    public static final RegistryObject<EntityType<SkeletonBowMasterEntity>> SKELETON_BOW_MASTER =
            ENTITY_TYPES.register("skeleton_bow_master",
                    () -> EntityType.Builder.of(SkeletonBowMasterEntity::new, MobCategory.MONSTER)
                            .sized(0.6F, 1.8F)
                            .fireImmune()
                            .build(new ResourceLocation(SkeletonBowMaster.MODID, "skeleton_bow_master").toString()));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }

}
