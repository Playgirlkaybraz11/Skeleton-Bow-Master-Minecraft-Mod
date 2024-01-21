package com.leecrafts.bowmaster.item;

import com.leecrafts.bowmaster.SkeletonBowMaster;
import com.leecrafts.bowmaster.entity.ModEntityTypes;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, SkeletonBowMaster.MODID);

    public static final RegistryObject<Item> SKELETON_BOW_MASTER_SPAWN_EGG = ITEMS.register("skeleton_bow_master_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntityTypes.SKELETON_BOW_MASTER, 0x9e9e9e, 0x9e9e9e,
                    new Item.Properties()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

}
